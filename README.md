# OpenMRS Billing Module

The OpenMRS Billing Module is a comprehensive billing and payment management system for OpenMRS. It provides a complete solution for healthcare facilities to manage patient billing, process payments, generate receipts, and track financial transactions.

## Key Features

### Bill Management

Create and manage patient bills with multiple line items, track bills and individual line items through their lifecycle (pending, paid, exempted, refunded, etc.), and associate bills with patient visits. Supports bill adjustments with full audit trail.

### Bill Discounts

Apply line-item or whole-bill discounts with a full audit trail. Discounts can be filtered on, and are linked back to the bill so they are purged along with it. Enabled by default; toggle via `billing.discountEnabled`.

### Bill Refunds

Request and approve refunds against paid bills with proper privilege controls. Refunds are surfaced in the bill representation and can be filtered on via the `refundStatus` parameter on `GET /bill`. Enabled by default; toggle via `billing.refundEnabled`.

### Patient Payment Status

Resolve a patient's overall payment status (paid / pending / exempted / no active bills) via a pluggable resolver, configurable through the `billing.patientPaymentStatusResolver` global property. Designed to surface payment state without blocking access to clinical forms.

### Payment Processing

Process payments using multiple payment modes (cash, insurance, mobile money, credit/debit cards, custom modes) with support for partial payments, payment attributes, automatic change calculation, and per-payment cashier attribution.

### Receipt Generation

Generate and print receipts using configurable Jasper Report templates with sequential or custom receipt numbering, configurable logos, and reprint capabilities with privilege controls.

### Cash Point Management

Manage multiple cashier stations/locations with cash point assignment for cashiers and location-based transaction tracking.

### Cashier Timesheets

Track cashier shifts with clock in/out functionality, configurable timesheet validation for bill creation, auto-close timesheets, and shift-based reporting.

### Item and Pricing Management

Manage billable healthcare services, configure item prices with price history tracking, and integrate with OpenMRS Stock Management module.

### Billing Exemptions

Exempt a service or commodity from billing when an order for it is placed. Each exemption targets a concept and carries one or more JavaScript rules, evaluated against the patient's age (`patientAge`) and basic order details (`order.uuid`, `order.conceptId`), so age-based and order-based criteria are supported.

### Financial Reports

Generate shift summary reports, daily shift summaries, department collections, department revenue, and payments by payment mode reports.

### Order Integration

Automatically generate billable items and bill line items from clinical orders, including medication orders, with order-to-bill line item mapping.

### REST API & Integration

Provides REST API endpoints at `/rest/v1/billing/*` for bills, payments, payment modes, billable services, cash points, item prices, discounts, refunds, and patient payment status. Includes patient dashboard integration for OpenMRS 2.x with configurable bill history widget. Supports English, French, and Spanish translations.

### FHIR Invoice Support

Exposes bills as FHIR `Invoice` resources via the `fhir` submodule, built against the `fhir2` module. Supports OpenMRS Platform 2.5, 2.6, and 2.7 FHIR variants.

## Requirements

- **OpenMRS Platform**: 2.7.8 (built and tested against; module `require_version` follows the build property)
- **Java Version**: 1.8 or higher
- **Required Modules**:
  - Web Services REST Module 2.9+
  - Stock Management Module 1.4.0+
  - FHIR2 Module 2.4.0+
  - Event Module 4.0.0+
- **Optional Modules**:
  - IDGen Module 2.8+
  - UI Framework Module
  - App Framework Module
  - Provider Management Module
  - UI Commons Module

## Installation

1. Download the latest release from the [releases page](https://github.com/openmrs/openmrs-module-billing/releases) or the [OpenMRS Add Ons](https://addons.openmrs.org/) directory
2. Install the required dependency modules (webservices.rest, stockmanagement, fhir2, event)
3. Upload and start the Billing module via the OpenMRS Module Management interface
4. Configure global properties and module settings
5. Set up payment modes, cash points, and billable items
6. Assign appropriate privileges to user roles

## Configuration

The billing module can be configured through a **content package** that the [Initializer module](https://github.com/mekomsolutions/openmrs-module-initializer) 2.12.0 or later applies when the server starts.

Billing configuration lives in your content package under `configuration/backend_configuration/`:

**The folder names must match exactly.**

```
configuration/backend_configuration/
├── globalproperties/billing.xml
├── billableservices/billableServices.csv
├── paymentmodes/paymentModes.csv
├── cashpoints/cashPoints.csv
└── cashieritemprices/cashierItemPrices.csv
```

### Global properties

Every `<globalProperty>` declared with the `${project.parent.artifactId}` prefix in this repository's `omod/src/main/resources/config.xml` resolves to `billing.*` and goes in `globalproperties/billing.xml`. The file can have any name, as long as it has a `.xml` extension and sits inside the `globalproperties` folder.

Example -

```xml
<config>
    <globalProperties>
        <globalProperty>
            <property>billing.systemReceiptNumberGenerator</property>
            <value>org.openmrs.module.billing.api.SequentialReceiptNumberGenerator</value>
        </globalProperty>
        <globalProperty>
            <property>billing.receipt.logoPath</property>
            <value>/openmrs/assets/logo.png</value>
        </globalProperty>
        <globalProperty>
            <property>billing.currencySymbol</property>
            <value>KES</value>
        </globalProperty>
    </globalProperties>
</config>
```

See [Global properties reference](#global-properties-reference) below for the full list.

### Billable services

Services a facility can charge for. `Concept` and `Service Type` are references to existing concepts — by UUID, name,
or a `source:code` mapping.

```csv
Uuid, Void/Retire, Service Name, Short Name, Concept, Service Type, Service Status
44ebd6cd-04ad-4eba-8ce1-0de4564bfd17,, Antenatal care, ANTC, 1592AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, Antenatal Services, Enabled
360fab13-d92b-4a9f-ad4e-0ac223e7f54c,, OPD consultation, OPDC, 160542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, Outpatient Services, Enabled
```

`Service Status` defaults to `Enabled` when left blank.

### Payment modes

The forms of payment a cashier can accept. `attributes` is optional and defines the extra fields captured with a
payment: a semicolon-separated list, each entry split by `::` into `name`, `format`, `regex` and `required`.

```csv
Uuid, Void/Retire, name, attributes
526bf278-ba81-4436-b867-c2f6641d060a,, Cash,
2b1b9aae-5d35-43dd-9214-3fd370fd7737,, Bank transfer,
e168c141-f5fd-4eec-bd3e-633bed1c9606,, Mobile money, Phone Number::Text::::True;Reference
```

### Cash points

The cashier stations bills are raised at. `location` references an existing location, so define your locations in the
same content package.

```csv
Uuid, Void/Retire, name, description, location
54065383-b4d4-42d2-af4d-d250a1fd2590,, OPD Cash Point, OPD cash point for billing, Opd Clinic
ba685651-ed3b-4e63-9b35-78893060758a,, IPD Cash Point, IPD cash point for billing, Inpatient Ward
```

### Item prices

What each service or stock item costs under a given payment mode. Set **exactly one** of `Stock Item` or
`Billable Service` per row — rows with both, or neither, are rejected.

```csv
Uuid, Void/Retire, Name, Price, Payment Mode, Stock Item, Billable Service
c1c1c1c1-0000-0000-0000-000000000001,, ANC Service Price, 150.00, 526bf278-ba81-4436-b867-c2f6641d060a,, 44ebd6cd-04ad-4eba-8ce1-0de4564bfd17
c1c1c1c1-0000-0000-0000-000000000004,, Paracetamol Item Price, 20.00, 526bf278-ba81-4436-b867-c2f6641d060a, b2b2b2b2-0000-0000-0000-000000000001,
```

`Payment Mode`, `Stock Item` and `Billable Service` are all matched by UUID. Which of the two an order is priced
through depends on its type — see [Automatic billing for orders](#automatic-billing-for-orders) below.

For the full header-by-header documentation of each domain, see the Initializer docs for
[billableservices](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/billableservices.md),
[paymentmodes](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/paymentmodes.md),
[cashpoints](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/cashpoints.md) and
[cashieritemprices](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/cashieritemprices.md).

#### Automatic billing for orders

Test and drug orders are billed automatically — nobody has to raise the bill in the cashier app. Saving an order
publishes a `CREATED` event through the Event module, and the billing module reacts to it in a daemon thread: it picks
the first registered billing strategy that supports the order, works out the price, and saves a pending bill with a
single line item for that order.

Two strategies ship with the module, matched on the order's Java class: `org.openmrs.TestOrder` and
`org.openmrs.DrugOrder`. They differ only in where the price comes from. Orders of any other class — including order
types your distribution has defined against plain `org.openmrs.Order` — are not billed automatically; see
[Customising the strategies](#customising-the-strategies) for how to add support for them.

Renewing or revising an order re-bills it, voiding the line item from the previous order first; discontinuing an order
voids its line item without creating a new bill. An order that has already been billed is never billed twice.

##### Test orders — priced through a billable service

This covers labs on a stock OpenMRS install, where the Test Order type is defined against `org.openmrs.TestOrder`.
Check your `ordertypes` configuration first: distributions that define their own Lab or Radiology order types can
map them to plain `org.openmrs.Order`, and those are not picked up.

A test order is priced through its concept. Define a billable service whose `Concept` is the ordered concept and whose
`Service Status` is `Enabled` — a retired or disabled service is not matched — then add an item price row pointing at
that service:

```csv
# billableservices/billableServices.csv
Uuid, Void/Retire, Service Name, Short Name, Concept, Service Type, Service Status
7f1cbf6a-1b1b-4f24-9d0a-2e6e3f2b1a01,, Malaria smear, MALS, 32AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, Laboratory Services, Enabled
```

```csv
# cashieritemprices/cashierItemPrices.csv
Uuid, Void/Retire, Name, Price, Payment Mode, Stock Item, Billable Service
c1c1c1c1-0000-0000-0000-000000000010,, Malaria smear price, 200.00, 526bf278-ba81-4436-b867-c2f6641d060a,, 7f1cbf6a-1b1b-4f24-9d0a-2e6e3f2b1a01
```

The line item quantity is always 1. Both rows are needed: a concept with no enabled billable service is not billed at
all, and a service with no item price is billed at `0.00`. A lab showing up free on a bill usually means the price row
is missing.

##### Drug orders — priced through a stock item

Drug orders can be priced through the **stock item** linked to the ordered drug in the Stock Management module or from your `cashieritemprices` configuration.

When a drug order is saved, the module finds the stock item for the drug, then takes the first of these that exists:

1. a `cashieritemprices` row whose `Stock Item` is that stock item — the price you configured, if not,
2. the stock item's purchase price, as recorded in the Stock Management module, if not
3. `0.00`.

So pricing drugs in `cashieritemprices` is optional. Leave the rows out and patients are billed the purchase price
Stock Management already holds.

```csv
# cashieritemprices/cashierItemPrices.csv
Uuid, Void/Retire, Name, Price, Payment Mode, Stock Item, Billable Service
c1c1c1c1-0000-0000-0000-000000000011,, Paracetamol 500mg price, 20.00, 526bf278-ba81-4436-b867-c2f6641d060a, b2b2b2b2-0000-0000-0000-000000000001,
```

Either way a stock item has to be linked to the drug: if none is, or if the order is free-text with no drug set, no
line item is created. The line item quantity comes from the quantity on the drug order.

When several prices exist for the same stock item or billable service, automatic billing takes the most recently
created one; it does not pick by `Payment Mode`.

##### Customising the strategies

Both strategies are replaceable. A strategy is any Spring bean implementing
`org.openmrs.module.billing.api.billing.OrderBillingStrategy`; the module collects every registered implementation,
sorts them by `getOrder()` (lowest value first) and hands the order to the first one whose `supports()` returns true.
The two shipped strategies return `Ordered.LOWEST_PRECEDENCE`, so any bean returning a lower value is consulted before
them.

Pick the base class that matches how much you want to change:

- **Change how a line item is priced or built** — extend `AbstractDefaultOrderBillingStrategy` and implement
  `createBillLineItem(Order)`. Duplicate detection, exemption evaluation, void-on-revise, void-on-discontinue and bill
  creation all stay as they are. This is the right level for a different price source, a different quantity rule, or
  picking between several stock items for one drug.
- **Change who the bill is attributed to** — override `resolveCashier(Order)` and `resolveCashPoint()`. The defaults
  use the orderer as the cashier and the first non-retired cash point, which is rarely what a multi-site facility
  wants.
- **Change what happens per order action** — extend `AbstractOrderBillingStrategy` and implement `handleNewOrder`,
  `handleRenewOrder`, `handleRevisedOrder` and `handleDiscontinuedOrder` yourself, or implement `OrderBillingStrategy`
  directly for full control. Call `setSupportedActions(...)` to limit which of `NEW`, `RENEW`, `REVISE` and
  `DISCONTINUE` the strategy responds to.

Implementing `supportsOrder(Order)` for a type nothing currently handles — a radiology or referral order, say — adds
automatic billing for that type rather than overriding anything.

```java
public class InsuranceDrugOrderBillingStrategy extends AbstractDefaultOrderBillingStrategy {

    @Override
    protected boolean supportsOrder(Order order) {
        return order instanceof DrugOrder;
    }

    @Override
    protected Optional<BillLineItem> createBillLineItem(Order order) {
        // resolve the price however your implementation needs to
    }

    @Override
    public int getOrder() {
        return 0; // beats the shipped strategy's LOWEST_PRECEDENCE
    }
}
```

Register it in your own module's `moduleApplicationContext.xml` and it is picked up on startup:

```xml
<bean id="insuranceDrugOrderBillingStrategy"
      class="org.openmrs.module.myfacility.billing.InsuranceDrugOrderBillingStrategy"/>
```

### Assign privileges

The module defines granular privileges for bill management (view, manage, adjust, purge, refund, reprint), discount
management (view, manage, approve), refund management (view, request, approve, complete), metadata management (view,
manage, purge), timesheet management (view, manage, purge), and app access for OpenMRS 2.x (cashier app, tasks,
reports). See `omod/src/main/resources/config.xml` for the complete list.

Assign them to roles through the Initializer `roles` and `privileges` domains so role setup ships with the rest of your
configuration.

## Global properties reference

**Receipts and reports**

| Property                               | Default                                                           | Description                                                                                                                       |
| -------------------------------------- | ----------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `billing.defaultReceiptReportId`       | —                                                                 | ID of the Jasper report used to generate a receipt on the Bill page                                                               |
| `billing.defaultShiftReportId`         | —                                                                 | ID of the Jasper cashier shift report                                                                                             |
| `billing.receipt.logoPath`             | —                                                                 | Path to the logo image printed on receipts                                                                                        |
| `billing.currencySymbol`               | —                                                                 | Currency shown on receipts (e.g. `USD`, `KES`, or custom text). Falls back to the locale default when unset                       |
| `billing.systemReceiptNumberGenerator` | `org.openmrs.module.billing.api.SequentialReceiptNumberGenerator` | Fully-qualified class name of the receipt number generator. See [Receipt numbering](#receipt-numbering) below                     |
| `billing.sequenceBlockSize`            | `100`                                                             | Receipt sequence values reserved per database round-trip. Must be at least one. See [Receipt numbering](#receipt-numbering) below |

**Bill rounding**

| Property                 | Default | Description                                              |
| ------------------------ | ------- | -------------------------------------------------------- |
| `billing.roundingMode`   | —       | How bill totals are rounded: `FLOOR`, `MID` or `CEILING` |
| `billing.roundToNearest` | —       | Nearest unit to round to. May be a decimal               |
| `billing.roundingItemId` | —       | ID of the item used to account for bill total rounding   |
| `billing.roundingDeptId` | —       | ID of the department the rounding item belongs to        |

**Bill behaviour**

| Property                               | Default | Description                                                                                                                                                                   |
| -------------------------------------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `billing.timesheetRequired`            | —       | Require an active timesheet before a bill can be created                                                                                                                      |
| `billing.allowBillAdjustments`         | `true`  | Enable bill adjustments                                                                                                                                                       |
| `billing.adjustmentReasonField`        | —       | Require a reason when adjusting a bill                                                                                                                                        |
| `billing.autofillPaymentAmount`        | `false` | Pre-fill the payment amount with the remaining balance                                                                                                                        |
| `billing.discountEnabled`              | `true`  | Enable bill discount management                                                                                                                                               |
| `billing.refundEnabled`                | `true`  | Enable refund requests and approval                                                                                                                                           |
| `billing.patientDashboard2BillCount`   | `5`     | Bills shown on the OpenMRS 2.x patient dashboard. Falls back to 4 if the property is blank or non-numeric                                                                     |
| `billing.patientPaymentStatusResolver` | —       | Fully-qualified class name of the patient payment status resolver. Blank uses the built-in one. See [Patient payment status resolver](#patient-payment-status-resolver) below |

**Financial reports**

| Property                                | Default | Description                               |
| --------------------------------------- | ------- | ----------------------------------------- |
| `billing.reports.departmentCollections` | —       | ID of the Department Collections report   |
| `billing.reports.departmentRevenue`     | —       | ID of the Department Revenue report       |
| `billing.reports.shiftSummary`          | —       | ID of the Shift Summary report            |
| `billing.reports.dailyShiftSummary`     | —       | ID of the Daily Shift Summary report      |
| `billing.reports.paymentsByPaymentMode` | —       | ID of the Payments by Payment Mode report |

### Receipt numbering

The default generator hands out sequential receipt numbers. To avoid a database round-trip per bill it reserves a block
of `billing.sequenceBlockSize` values at a time and serves them from memory. Receipt numbers are always unique, but
values can be skipped: restarting the server discards whatever is left of the current block, losing up to
`blockSize - 1` values per sequence group, and a transaction that rolls back burns the value it took. Larger blocks
reduce contention under load; smaller blocks reduce the gaps.

Reserved blocks are held per JVM. If you edit or purge a sequence value on a clustered installation, the other nodes
keep serving the blocks they have already reserved, so only do it on every node at once or while the other nodes are
stopped.

To use your own numbering scheme, implement `org.openmrs.module.billing.api.IReceiptNumberGenerator` in another module
and set `billing.systemReceiptNumberGenerator` to its fully-qualified class name.

### Patient payment status resolver

`billing.patientPaymentStatusResolver` selects how a patient's overall payment status is derived. Leave it blank to use
the built-in resolver, which reads existing bill records. To override it, implement
`org.openmrs.module.billing.api.PatientPaymentStatusResolver`, register your implementation as a Spring component in
your own module so it is discoverable, and set the property to its fully-qualified class name.

## Documentation

- **User Documentation**: [OpenMRS Billing Module Wiki](https://openmrs.atlassian.net/wiki/x/XIeEAQ)

## Development

### Building the Module

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Code Formatting

The project uses the OpenMRS code formatting conventions:

```bash
mvn formatter:format
```

## Contributing

We welcome contributions! Please:

1. Fork the repository
2. Branch off from `main`
3. Make your changes following OpenMRS coding conventions
4. Write tests for new functionality

## Credits

This module was originally developed by [OpenHMIS](https://openmrs.atlassian.net/wiki/x/kACXAQ) as the Cashier Module and is now maintained by the OpenMRS community.

## License

This module is licensed under the OpenMRS Public License. See [LICENSE.txt](LICENSE.txt) for details.

## Support

For questions, feedback, or issues:

- Post in the #openmrs-billing channel in the OpenMRS Slack community
- Post on the [OpenMRS Talk](https://talk.openmrs.org/) community forum
- Create an issue in this repository

---
