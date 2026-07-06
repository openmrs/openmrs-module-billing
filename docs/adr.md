# Architecture Decision Records

This document captures the key architectural decisions for the OpenMRS Billing module. The first
set records the **QueryStore / chart-search integration** (the optional `billing-querystore` omod,
[PR #186](https://github.com/openmrs/openmrs-module-billing/pull/186)), which lets a clinician-facing
semantic/keyword chart search (e.g. [chartsearchai](https://github.com/openmrs/openmrs-module-chartsearchai))
retrieve a patient's bills.

## Background

The [OpenMRS QueryStore module](https://github.com/openmrs/openmrs-module-querystore) maintains a
read-optimized projection of clinical data for semantic / free-text / kNN search, and exposes a
`ResourceTypeProvider` SPI (its own [ADR Decision 13](https://github.com/openmrs/openmrs-module-querystore/blob/main/docs/adr.md))
for modules to contribute custom resource types. This integration makes the Billing module such a
provider so that bills, discounts, and refunds become searchable alongside core clinical data.

## Conventions

- Decisions are numbered and append-only once accepted; a superseded decision is marked and points
  at its replacement rather than being edited away.
- These decisions concern the integration only. The base Billing module (api/omod/fhir) is
  intentionally left unchanged by them — see Decision 2.

## Table of Contents

1. [Integrate through the QueryStore ResourceTypeProvider SPI](#decision-1-integrate-through-the-querystore-resourcetypeprovider-spi)
2. [Ship as a separate, optional, profile-gated omod](#decision-2-ship-as-a-separate-optional-profile-gated-omod)
3. [Which billing records to index, and at what granularity](#decision-3-which-billing-records-to-index-and-at-what-granularity)
4. [billing_bill folds only fields that stay fresh on a bill save](#decision-4-billing_bill-folds-only-fields-that-stay-fresh-on-a-bill-save)
5. [Two sync paths: AOP service events for bills, Hibernate DB events for discounts and refunds](#decision-5-two-sync-paths-aop-service-events-for-bills-hibernate-db-events-for-discounts-and-refunds)
6. [Require exact (SNAPSHOT) dependency versions](#decision-6-require-exact-snapshot-dependency-versions)

---

## Decision 1: Integrate through the QueryStore ResourceTypeProvider SPI

### Status
Accepted

### Context
chart-search needs billing records available in the query store. QueryStore is "events-first": its
`CoreServiceEventListener` projects any entity for which a `ClinicalRecordSerializer` is registered,
and it offers a `ResourceTypeProvider` SPI (serializer + optional bootstrapper) discovered via
`Context.getRegisteredComponents(...)`. Its SPI walkthrough even uses `billing_bill` as the running
example.

### Decision
Implement the integration purely as SPI contributions — per resource type a
`ClinicalRecordSerializer` (mapping the entity to a `QueryDocument`), a `HibernateTypeBootstrapper`
(one-time backfill of existing rows), and a `ResourceTypeProvider` bundling them — registered as
Spring beans in the submodule's `moduleApplicationContext.xml`. We do **not** build a bespoke
event/publish mechanism; steady-state indexing rides on the events QueryStore already consumes
(see Decision 5), and initial backfill is admin-triggered via QueryStore's `reindex` endpoint.

### Consequences
- Minimal, declarative integration; QueryStore owns the store, embedding, schema lifecycle, search,
  and authorization.
- The module compiles against `querystore-api` (provided scope) and couples to its SPI contract.
- "Register a serializer" — not "publish events" — is the mental model for maintainers.

---

## Decision 2: Ship as a separate, optional, profile-gated omod

### Status
Accepted

### Context
QueryStore and the core `*ServiceEvent` API it relies on require the OpenMRS platform **2.9** (and,
in practice, an Elasticsearch/Lucene backend plus the querystore module), whereas the base Billing
module targets **2.7.8** and is widely deployed on older platforms. The integration must not force
every Billing deployment onto that stack.

### Decision
Put the integration in a new `querystore/` Maven module that builds its own `billing-querystore`
omod, hard-requiring `billing` + `querystore` + platform 2.9. It is **not** in the default
`<modules>`; it builds only under a `querystore` Maven profile (`mvn -Pquerystore`). The base
`billing.omod` and the default build are unchanged.

### Alternatives considered
- **Bundle the SPI beans into the existing `omod`.** Rejected: with `provided`-scope `querystore-api`
  the beans fail to instantiate when querystore is absent, and hard-requiring querystore would push
  every Billing install onto platform 2.9 + Elasticsearch.
- **A standalone repository.** Rejected: the code is billing-domain-specific (it serializes Bill /
  BillDiscount / BillRefund), so it belongs with the module; a separate repo adds release/version
  coordination for no benefit.

### Consequences
- Deployers running QueryStore drop in one extra omod; everyone else is unaffected.
- The reactor produces two omods; the `querystore` profile compiles against 2.9 / querystore
  SNAPSHOTs (see Decision 6) without disturbing the 2.7.8 default build.

---

## Decision 3: Which billing records to index, and at what granularity

### Status
Accepted

### Context
The selection question is "what would help a clinician reading the patient chart" — a retrieval aid,
not a full financial export. Two hard filters narrow the billing domain's ~15 persistent types:

- **Patient-scoped.** The document must belong to a patient's chart; this drops facility/ops records.
- **`OpenmrsData`, not `OpenmrsMetadata`.** QueryStore's projection pipeline only handles
  `OpenmrsData` (its consumer needs `getVoided()` and routes voided → delete), so metadata types
  cannot be indexed even if we wanted them.

A third, softer consideration is *marginal value*: billed drugs/tests often already appear in the
chart as orders/obs, so billing earns its place mainly where it adds what those don't — services
billed but not order-entered (procedures, bed/consultation charges), and the **financial** dimension
(an unpaid balance is a real barrier to care; a fee waiver is social context).

### Decision
Index three patient-scoped `OpenmrsData` types, each justified by what it tells a clinician:

- **`billing_bill`** — the anchor. Its line items are a proxy for the services / drugs / tests the
  patient was charged for (i.e. what care they received), and its status + total/paid answer "does
  this patient owe money?". `Bill` line items and payments are **folded into** this one document
  (they cascade with the bill and have no independent lifecycle), so the searchable text leads with
  the billed items.
- **`billing_discount`** — a fee waiver / discount is patient-relevant because it often signals
  enrollment in a subsidized programme (HIV, TB, under-5, indigent) or financial hardship. Indexed
  as its **own** type because its lifecycle (PENDING → APPROVED/REJECTED) changes independently of
  the bill.
- **`billing_refund`** — the weakest clinical signal (mostly administrative), included to complete
  the record; near-free to add since it already carries its own lifecycle (REQUESTED → APPROVED →
  COMPLETED).

Excluded, with reasons:
- **Not patient-scoped:** timesheets (cashier / shift), sequence generators and group sequences
  (infra).
- **Facility configuration / reference data:** cash points, payment modes (+ attribute types), the
  billable-service catalog, item prices — they describe the facility, not a patient's care.
- **Policy, not a per-patient record:** exemption *rules* (`BillExemption` / `BillExemptionRule`)
  define *who* is exempt, not a given patient's exemption event.
- Several of the above are `OpenmrsMetadata`, so the technical filter excludes them regardless.

### Consequences
- High signal-to-noise for chart search: a clinician can find what a patient was charged for, whether
  there is an unpaid balance, and whether a fee waiver or refund applies.
- Complementary to — not duplicative of — the orders/obs already indexed: billing adds the financial
  view and any services billed outside order entry.
- Fewer types to serialize and keep in sync; the excluded config/metadata would add noise and mostly
  isn't projectable anyway.

### Example chart-search queries
Natural-language questions a clinician asks chart search (`POST /ws/rest/v1/chartsearchai/search`
with `{"question": ..., "patient": ...}`), and the resource each is expected to surface. The
*italicized* ones were exercised during verification (see the PR's verification comments) and
returned the corresponding `resourceType`.

- **`billing_bill`** — *"What has this patient been billed for, and what is the outstanding
  balance?"*; "Does this patient have any unpaid bills?"; "Was the patient charged for a malaria
  test / X-ray / consultation?"
- **`billing_discount`** — *"Does this patient have any billing discounts or fee waivers, and for
  how much?"*; "Is there a hardship or subsidized-programme waiver on this patient's bills?"
- **`billing_refund`** — *"Has this patient had any bill refunds? What amount and reason?"*; "Was any
  charge reversed or refunded for this patient?"

Retrieval is semantic, not keyword-exact, so phrasing varies freely — "money owed", "charges",
"waiver", "reimbursement" route to the right documents without matching the stored text verbatim.
(A single question may cite more than one type — e.g. a refund question surfaced both
`billing_refund` and the parent `billing_bill`, whose status had flipped to REFUND_REQUESTED.)

---

## Decision 4: billing_bill folds only fields that stay fresh on a bill save

### Status
Accepted

### Context
QueryStore re-projects `billing_bill` when a bill is (re)saved. A folded field is only kept current
if every mutation that changes it re-saves the bill. `BillDiscountService.saveBillDiscount` persists
the discount alone and does **not** re-save the parent bill, so a denormalized "amount after
discount" / outstanding balance on `billing_bill` would silently overstate what the patient owes
until the bill's next save.

### Decision
`billing_bill` exposes only bill-aggregate-derived fields that are refreshed on every `saveBill`:
the raw (pre-discount) line-item **total**, **amount paid**, **status**, cash point, visit, and the
billed-service list. It does **not** denormalize a discount-adjusted total or balance; discount
detail lives on `billing_discount`. Consumers derive "outstanding" from total + amount paid.

### Consequences
- No silently-wrong financial figure in the index.
- Confirmed in live testing: the LLM correctly answered "outstanding balance 500.00" from the emitted
  `Total: 500, paid: 0` text.
- Residual, documented eventual-consistency: line-item voids via the dedicated `voidBillLineItem`
  endpoint lag until the bill's next save.

---

## Decision 5: Two sync paths: AOP service events for bills, Hibernate DB events for discounts and refunds

### Status
Accepted

### Context
openmrs-core #6084 (2.9) publishes change events at two layers: (a) an **AOP** advice
(`OpenmrsServiceEventAdvice`, pointcut `target(org.openmrs.api.OpenmrsService)`) emits
`SaveServiceEvent`/`VoidServiceEvent`/… for `OpenmrsService` `save*`/`void*`/… calls; (b) a
**non-AOP** Hibernate interceptor (`EventInterceptor`) emits `SaveDbEvent`/`DeleteDbEvent` for
*every* persisted entity, in-transaction on the flush thread. QueryStore's consumer subscribes to
the AOP service events. `BillService extends OpenmrsService`, but `BillDiscountService` and
`BillRefundService` are plain interfaces — so the AOP advice never fires for `saveBillDiscount`/
`saveBillRefund`, and those types would live-sync from nothing.

### Decision
- `billing_bill`: rely on the AOP `*ServiceEvent`s QueryStore already consumes (no code — `saveBill`
  qualifies).
- `billing_discount` / `billing_refund`: add a `BillChildDbEventListener` in the submodule that
  consumes core's **non-AOP** `SaveDbEvent`/`DeleteDbEvent`, filters to `BillDiscount`/`BillRefund`,
  and drives the same QueryStore projection pipeline (`RecordProjector` + the reachable
  `querystore.sync.*` beans) the service consumer uses. `Bill` is deliberately ignored there (it
  already syncs via its service event).

### Alternatives considered
- **Make `BillDiscountService` / `BillRefundService` extend `OpenmrsService`.** Rejected for this PR:
  it changes two public base-Billing services and puts them on the full core advice chain — a
  base-module behavior change outside the integration's scope. (Left as a possible future
  simplification.)
- **Publish synthetic `SaveServiceEvent`s from the DB-event handler.** Rejected: it would broadcast
  to any other service-event consumer and double the outbox signal for those entities.
- **Fold discounts/refunds into `billing_bill`.** Rejected — see Decision 4 (staleness).

### Consequences
- All three types live-sync with **no base-Billing change**; verified live on a 2.9 standalone
  (new discount/refund retrievable via chart search within seconds, no reindex).
- The listener couples to QueryStore-internal seams (`RecordProjector`, the `querystore.sync.*` bean
  ids) — a coupling the SPI explicitly sanctions for "a service that doesn't qualify contributes its
  own event listener."
- The DB-event listener is invoked for every entity save; it exits on a cheap `instanceof` filter.

---

## Decision 6: Require exact (SNAPSHOT) dependency versions

### Status
Accepted

### Context
OpenMRS's module/platform version matching ranks `X.Y.Z-SNAPSHOT` **below** a bare `X.Y.Z`. Because
platform 2.9, querystore, and the sibling billing build are all pre-release SNAPSHOTs, bare
requirements (`require_version 2.9.0`, `require_module querystore 1.0` / `billing 2.4`) are left
*unsatisfied* by the running SNAPSHOT builds, and the module silently fails to start.

### Decision
Filter the `config.xml` requirements from the module's own Maven properties —
`${openmrsPlatformVersion}` (2.9.0-SNAPSHOT), `${querystoreVersion}` (1.0.0-SNAPSHOT), and
`${project.parent.version}` (the sibling billing version) — matching how `chartsearchai` requires
querystore (`1.0.0-SNAPSHOT`).

### Consequences
- The module loads on the current 2.9 / querystore SNAPSHOT stack (found only by deploying to a real
  2.9 standalone).
- At release time the same properties resolve to release versions, so the requirements track
  automatically.
