# ADR 0001: Querystore indexing for billing resources

- **Status:** Accepted
- **Date:** 2026-05-20

## Table of contents

- [Context](#context)
- [Decisions](#decisions)
  - [D1. Indexed resource types](#d1-indexed-resource-types)
  - [D2. Why `BillableService` is NOT a resource type](#d2-why-billableservice-is-not-a-resource-type)
  - [D3. Field names are part of the public contract](#d3-field-names-are-part-of-the-public-contract)
  - [D4. Shape contract: multi-valued metadata is `List<String>`, not comma-joined](#d4-shape-contract-multi-valued-metadata-is-liststring-not-comma-joined)
  - [D5. Multi-valued fields are sorted (TreeSet / TreeMap) for stable bytes](#d5-multi-valued-fields-are-sorted-treeset--treemap-for-stable-bytes)
  - [D6. `payment_modes` and `payment_mode_amounts` are parallel arrays](#d6-payment_modes-and-payment_mode_amounts-are-parallel-arrays)
  - [D7. Each query type has both an aggregate and a detail view](#d7-each-query-type-has-both-an-aggregate-and-a-detail-view)
  - [D8. Audit columns: shared `BillingAuditFields` helper](#d8-audit-columns-shared-billingauditfields-helper)
  - [D9. `BillingDisplayNames` is querystore-scoped](#d9-billingdisplaynames-is-querystore-scoped)
  - [D10. Trigger method names match the service interface verbatim](#d10-trigger-method-names-match-the-service-interface-verbatim)
  - [D11. AOP is intercepting outer calls only — internal `save()` is a self-call](#d11-aop-is-intercepting-outer-calls-only--internal-save-is-a-self-call)
  - [D12. Serializers and providers are `lazy-init="true"`](#d12-serializers-and-providers-are-lazy-inittrue)
  - [D13. Whitespace-only and empty values are skipped, not stored](#d13-whitespace-only-and-empty-values-are-skipped-not-stored)
  - [D14. Always-emitted vs conditional fields](#d14-always-emitted-vs-conditional-fields)
  - [D15. `getDate()` returns LocalDate; `created_at` is the full timestamp](#d15-getdate-returns-localdate-created_at-is-the-full-timestamp)
  - [D16. No bulk-reindex bootstrapper](#d16-no-bulk-reindex-bootstrapper)
- [Open issues (deferred, with failure modes)](#open-issues-deferred-with-failure-modes)
- [Consequences](#consequences)
  - [What this slice unlocks](#what-this-slice-unlocks)
  - [What this slice intentionally does NOT do](#what-this-slice-intentionally-does-not-do)
  - [Operational notes](#operational-notes)

## Context

The billing module persists `Bill`, `BillLineItem`, `Payment`, `BillDiscount`, `BillRefund`, `Timesheet`, `BillableService` and related entities. Free-text and structured search across this data — "find bills paid by Mobile Money", "approval queue for pending discounts", "find the bill for this lab order", "who was on duty between 2pm and 3pm" — was previously not feasible without scanning the persistence layer one row at a time.

The `querystore` module provides a patient-scoped search index over OpenMRS data via an SPI: modules contribute `ResourceTypeProvider` beans that expose a `ClinicalRecordSerializer<T>`, and an `AbstractIndexingAdvice<T extends BaseOpenmrsData>` AOP advice fires on configured service methods to keep the index in sync with the database. The billing module participates in that SPI to make its data searchable.

This ADR records the load-bearing decisions behind that participation. It does not catalogue every field — `BillingQueryStoreConstants` and the serializer source are the canonical references — but it does explain *why* the field shapes, lifecycle hooks, and abstractions are what they are. Anyone planning to add a new field, a new resource type, or a new consumer should read this first.

## Decisions

### D1. Indexed resource types

We expose four resource types to the querystore:

| Constant                     | Value                       | Patient-scoped? | Trigger service           |
| ---------------------------- | --------------------------- | --------------- | ------------------------- |
| `RESOURCE_TYPE_BILL`         | `billing_bill`              | Yes             | `BillService`             |
| `RESOURCE_TYPE_BILL_REFUND`  | `billing_bill_refund`       | Yes             | `BillRefundService`       |
| `RESOURCE_TYPE_BILL_DISCOUNT`| `billing_bill_discount`     | Yes             | `BillDiscountService`     |
| `RESOURCE_TYPE_TIMESHEET`    | `billing_timesheet`         | No (provider)   | `ITimesheetService`       |

`Timesheet` is provider-scoped: `getPatientUuid(...)` returns `null`. The querystore SPI permits patient-less documents for administrative data; they are still indexed under the resource type and queryable by `provider_uuid`, `cash_point_uuid`, `clock_in` / `clock_out`.

### D2. Why `BillableService` is NOT a resource type

`BillableService` extends `BaseChangeableOpenmrsMetadata` (catalog reference data), not `BaseOpenmrsData` (patient data). `AbstractIndexingAdvice<T extends BaseOpenmrsData>` enforces the bound at the type level; an indexing advice for `BillableService` won't compile against the current SPI.

We attempted the slice and rolled it back. Catalog search ("find every Lab-department service") remains unaddressed by the querystore. The fix when it lands is a *querystore* change — widening the SPI bound to admit `BaseChangeableOpenmrsMetadata` — not a billing change. Until then, callers use `BillableServiceService.findBillableServices(...)` directly.

### D3. Field names are part of the public contract

Field-name constants in `BillingQueryStoreConstants` are referenced from two sides: the serializer (which writes metadata) and any consumer (querystore queries, dashboards, integrations).

A typo on either side produces no compile error, no startup error, and no runtime exception — the schema is self-healing, so a misspelled key just creates a parallel column nobody queries against. The failure surfaces only as "stale rows in the read store" some time later.

Consequences:

- Renaming a field is a re-index event. Don't do it lightly.
- New consumers that filter on a field MUST use the constant, never a string literal.
- Constants live in one file (`BillingQueryStoreConstants`) so the canonical name set is auditable in one place.

### D4. Shape contract: multi-valued metadata is `List<String>`, not comma-joined

Querystore consumers can filter on `List<String>` fields with exact-match semantics. Comma-joined strings force substring matching, which conflates a service literally named `"X-Ray, Chest"` with two separate items.

When a field is plural by nature (`payment_modes`, `discount_statuses`, `line_item_statuses`, `order_uuids`, `adjusted_by_uuids`, `line_item_names`), the serializer emits `List<String>`. The convention matches `VisitRecordSerializer.FIELD_ENCOUNTER_UUIDS` and `AllergyRecordSerializer.FIELD_REACTIONS` in the querystore module.

The exception: `BillRefund` writes `line_item_names` as `Collections.singletonList(name)` even though refunds are line-scoped. The reason is uniformity — consumers branching on resource type but sharing the field key would otherwise get `String` from refund docs and `List<String>` from bill docs, with `ClassCastException` waiting downstream. The shape contract is documented on the constant.

### D5. Multi-valued fields are sorted (TreeSet / TreeMap) for stable bytes

`Bill.payments`, `Bill.discounts`, `Bill.adjustedBy` are `Set<>` collections with non-deterministic iteration order. Re-indexing the same logical state would emit different document bytes on each save, breaking snapshot tests, cache keys, and any consumer that checks document equality.

Sorting at serializer time (`TreeSet<String>` for distinct sorted, `TreeMap<String, BigDecimal>` for sorted-by-key-with-values) gives consumers a stable list per logical state without committing the source-side to any particular order.

Note: `discount_statuses` sorts alphabetically (`APPROVED` before `PENDING`), NOT in workflow order. The constant's comment makes this explicit because a reader might assume otherwise.

### D6. `payment_modes` and `payment_mode_amounts` are parallel arrays

Per-mode totals are emitted as a separate List<String> in the same TreeMap iteration order as `payment_modes`. Consumers zip the two arrays at query time:

```
payment_modes:        ["Cash", "Mobile Money"]
payment_mode_amounts: ["40.00", "50.00"]
```

This shape was chosen over a `Map<String, BigDecimal>` because the querystore backend (Lucene / Elasticsearch / MySQL) treats metadata values as scalars or lists; nested objects are not first-class. The parallel-arrays pattern stays within that constraint.

Two payments of the same mode are summed into one entry. Whitespace-only mode names are skipped (a tender mode literally named `"   "` would otherwise show up between Cash and Mobile Money on every dashboard).

### D7. Each query type has both an aggregate and a detail view

Some questions are best answered against the parent bill ("which bills have a pending discount?"). Others against the child resource ("show me every pending discount").

The slice indexes both:

- `Bill.discount_statuses` answers the bill-level presence question.
- `billing_bill_discount` documents carry the per-discount detail (type, value, amount, justification, initiator, approver).

The aggregate is a denormalized signal — voided discounts and discounts with `null` status are excluded so a once-rejected-then-rewritten discount doesn't leave a phantom status on the bill.

### D8. Audit columns: shared `BillingAuditFields` helper

OpenMRS `BaseOpenmrsData` carries seven audit columns: `dateCreated`, `dateChanged`, `dateVoided`, `creator`, `changedBy`, `voidedBy`, `voidReason`. We want all of them queryable across all four resource types so the natural follow-up questions — "who voided this and why?", "bills modified in the last hour" — can succeed against the index.

A shared package-private `BillingAuditFields.populate(QueryDocument, BaseOpenmrsData)` centralises emission. Inlining the same six writes in four serializers would invite drift: a future refactor adds a field to one serializer and forgets the others, and audit queries silently miss the new resource type.

`dateCreated` is also indexed as the document's `date` (LocalDate) by the parent serializer; `created_at` is the full timestamp for time-of-day filtering.

### D9. `BillingDisplayNames` is querystore-scoped

The helper that picks a display name for a `BillLineItem` (`BillableService.getName()` preferred, `StockItem.getCommonName()` fallback) is scoped to the querystore SPI by design.

It is intentionally NOT used by:
- `ReceiptGenerator` (which prefers `Drug.name` on printed receipts).
- The FHIR translator (which keys off `Concept` presence).

A future contributor seeing three places picking item names might "consolidate" them. That would silently change user-visible printed receipts. The header comment on `BillingDisplayNames` documents the scope; this ADR records it for posterity.

### D10. Trigger method names match the service interface verbatim

`AbstractIndexingAdvice` matches advised invocations by method *name* against a configured `triggerMethods()` set. A typo produces no compile error and no runtime exception — the advice simply never fires.

The reflection test `IndexingAdviceConfigTest` catches the typo class at unit-test time by checking every name in `TRIGGER_METHODS` and `PURGE_METHODS` resolves to a method on the target service interface. Adding a new trigger requires adding both the name to the advice and (if appropriate) a test case.

`PURGE_METHODS` is always a subset of `TRIGGER_METHODS` per the `AbstractIndexingAdvice` contract — a name in purge but not in trigger is unreachable.

### D11. AOP is intercepting outer calls only — internal `save()` is a self-call

When `BillDiscountServiceImpl.saveBillDiscount(...)` runs, the AOP proxy intercepts the outer call and fires `BillDiscountIndexingAdvice`. The same method then calls `touchParentBill(...)` which dispatches through `Context.getService(BillService.class)` — that goes through a proxy boundary, so `BillIndexingAdvice` also fires on the resulting `saveBill`. One save call by the user produces two index writes (one for the discount, one for the bill); both are correct.

In contrast, `voidEntity(...)` on the generic data-service surface internally calls `this.save(...)`. That is a self-call — the proxy is bypassed, the inner advice does NOT fire. This is why `TimesheetIndexingAdvice.TRIGGER_METHODS` includes `save`, `voidEntity`, `unvoidEntity`, AND `purge`: the void-flow only fires the `voidEntity` advice, not `save`. Listing both keeps the slice robust to upstream changes in the entity service.

### D12. Serializers and providers are `lazy-init="true"`

Their supertypes (`AbstractRecordSerializer`, `ResourceTypeProvider`) live in the querystore-api jar, which is declared as a hard `require_module` in `omod/config.xml`. So at runtime, the supertypes always exist on the classpath.

But other modules (notably `billing-fhir`) load this Spring context in their own tests *without* querystore-api on the test classpath — `provided` scope does not propagate to dependent modules' test classpaths. Eager bean instantiation would class-load the supertypes at context-load time and crash.

`lazy-init="true"` defers the load until querystore's `getBeansOfType(ResourceTypeProvider.class)` scan asks for them, by which time querystore-api is guaranteed to be present.

### D13. Whitespace-only and empty values are skipped, not stored

A field with an empty-string or whitespace-only value pollutes consumer queries: an `exists` filter returns true, equality filters match "nothing in particular", and dashboards show stray rows with no human-readable label.

The serializers all use `value != null && !value.trim().isEmpty()` for free-text fields (`adjustment_reason`, `cashier_name`, `mode.getName()`, `void_reason`). The constraint surfaces in tests like `serialize_shouldSkipPaymentsWithMissingMode` (covering null instanceType, empty name, and whitespace-only name in one suite).

### D14. Always-emitted vs conditional fields

Most optional references (cashier, cashpoint, visit, line-item UUID, initiator, approver) are *conditional* — the field is absent when the reference is null, and consumers use missing-field filters to find "bills with no cashier" etc.

Two fields are *always emitted*: `voided` and `receipt_printed`. They are booleans with sensible defaults (`false`) and are useful as primary filters ("show me only non-voided bills"). Always-emit means a single term filter works without an exists-clause.

The `Boolean.TRUE.equals(bill.getReceiptPrinted())` normalization on `receipt_printed` shields against transient/in-flight bills where the field is unset. `voided` doesn't need it because `Bill.hbm.xml` declares it `not-null`.

### D15. `getDate()` returns LocalDate; `created_at` is the full timestamp

`AbstractRecordSerializer.getDate()` returns `LocalDate`, used as the document's primary date for clinical-date queries. The full `dateCreated` timestamp is also emitted as a `created_at` metadata field so time-of-day filters work ("bills created between 2pm and 3pm today").

For `Timesheet`, `getDate()` returns the `clockIn` date (fallback `dateCreated`) — the natural calendar key for "who was on duty on 2026-05-20" — not the document creation date.

### D16. No bulk-reindex bootstrapper

Both `BillResourceTypeProvider.getBootstrapper()` and `BillDiscountResourceTypeProvider.getBootstrapper()` (and the other two) return `null`. The AOP advice projects ongoing mutations into the index, but pre-existing rows from before the slice deployed do not appear until each row is saved again.

This is a deliberate v1 scope decision. A bootstrapper would iterate the entire bill table, serialize each row, and push it to the index — useful for backfill but operationally heavy. We chose to skip it on the assumption that the querystore module's own reindex tooling can fill the gap when needed.

Consequence: a "list every bill in the index" query returns only post-deploy bills until a full reindex runs.

## Open issues (deferred, with failure modes)

These are real concerns the slice surfaces but does not fix. Each has a failure-mode sentence so future maintainers can judge urgency.

- **OLD bill status drift on adjustment.** When bill B is created to adjust bill A, `Bill.setBillAdjusted(...)` mutates A's `status` to `ADJUSTED` and adds B to A's `adjustedBy` set. Only B flows through `BillService.saveBill`, so the indexing advice never re-fires for A. If we ship without fixing this, A's indexed `status`, `adjusted_by_uuids`, and `adjustment_reason` stay frozen at the pre-adjustment values until A is saved again. Fix belongs in `Bill.setBillAdjusted` or the REST `setBillAdjusted` flow.

- **`TimesheetServiceImpl.closeOpenTimesheets` bypasses indexing.** The bulk-close path mutates `clockOut` and relies on Hibernate dirty-checking — no `save()` is called per row, so `TimesheetIndexingAdvice` never fires. If we ship without fixing this, the timesheet index keeps `clock_out=null` on rows the service just closed; "who is on duty now" queries return false positives until each row is individually re-saved. Fix is one line per iteration in `TimesheetServiceImpl`.

- **`bill.adjustedBy` and `bill.discounts` LAZY walks per save.** Adding `adjusted_by_uuids` and `discount_statuses` to the bill document means every `saveBill` now walks two LAZY collections. Per save this is bounded; on a backfill bootstrapper it would be O(bills × M extra SELECTs). Mitigation belongs at the model layer (`lazy="extra"`) or in the bootstrapper's page loader (join fetch).

- **`getDiscountAmount()` LAZY navigation on PERCENTAGE discounts.** For a percentage discount, the canonical computation dereferences `lineItem` or `bill` LAZY proxies. Each `saveBillDiscount` for a percentage discount initializes those proxies even though only scalars are read. Accepted: the model's `getDiscountAmount()` is the source of truth; bypassing it for the index risks divergence between the indexed amount and the live computation.

- **Renames of `BillableService.name` or `StockItem.commonName` leave bills stale.** No indexing advice fires on the catalog services, so existing bills keep the pre-rename name in their `line_item_names` until next save. Accepted: renames are rare; next save heals; a full reindex is the remediation when it matters.

- **Text-blob comma conflation.** The bill's `text` field joins line item names with `", "`. A service literally named `"X-Ray, Chest"` produces ambiguous text-blob bytes for full-text search. Accepted: the structured `List<String>` `line_item_names` field is unambiguous and is the primary signal; full-text is only one of several search paths.

## Consequences

### What this slice unlocks

- "Find bills paid by Mobile Money" — `payment_modes` contains the tender name.
- "Total Cash collected this week" — zip `payment_modes` and `payment_mode_amounts`.
- "Bills with a pending discount" — `discount_statuses` contains `PENDING`.
- "Approval queue for pending discounts" — query the `billing_bill_discount` resource type filtered by `status=PENDING`.
- "Find the bill for this lab order" — `order_uuids` contains the order's UUID.
- "Bills with a REFUND_REQUESTED line item" — `line_item_statuses` contains the workflow status.
- "Who voided this bill and why" — `voided_by_uuid` and `void_reason` are indexed across all four resource types.
- "Who is on duty between 2pm and 3pm" — query `billing_timesheet` filtered by `clock_in` and `clock_out`.
- "Find bills handled by cashier Mary" — `cashier_name` is denormalized.
- "Trace the adjustment chain" — `bill_adjusted_uuid` and `adjusted_by_uuids` link both directions.

### What this slice intentionally does NOT do

- Catalog search (`BillableService`). See D2.
- Bulk backfill of pre-deploy data. See D16.
- Patient name denormalization. The querystore module's own `PatientRecordSerializer` indexes patient documents; billing documents reference patients only by UUID.
- Time-of-day on the parent serializer's `date` field — we use a separate `created_at` field. See D15.

### Operational notes

- **One save call per save event.** Per-row N+1 lazy fetches are bounded by the document size (number of line items × ~2 SELECTs in the worst case). Acceptable for the per-save path; flag for any future bootstrapper.
- **Field names are public.** A rename requires a coordinated re-index. The constant file is the canonical name set; see D3.
- **The `IndexingAdviceConfigTest` reflection test is load-bearing.** It catches trigger/purge name typos before they ship. Removing it disables a class of silent bug.
