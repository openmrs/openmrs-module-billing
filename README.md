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

Configure automated billing exemptions based on patient attributes with support for age-based, location-based, and custom exemption rules.

### Financial Reports

Generate shift summary reports, daily shift summaries, department collections, department revenue, and payments by payment mode reports.

### Order Integration

Automatically generate billable items and bill line items from clinical orders, including medication orders, with order-to-bill line item mapping.

### REST API & Integration

Provides REST API endpoints at `/rest/v1/billing/*` for bills, payments, payment modes, billable services, cash points, timesheets, item prices, discounts, refunds, and patient payment status. Includes patient dashboard integration for OpenMRS 2.x with configurable bill history widget. Supports English, French, and Spanish translations.

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
2. Install the required dependency modules (webservices.rest, stockmanagement)
3. Upload and start the Billing module via the OpenMRS Module Management interface
4. Configure global properties and module settings
5. Set up payment modes, cash points, and billable items
6. Assign appropriate privileges to user roles

## Configuration

The billing module can be configured through a **content package** that the [Initializer module](https://github.com/mekomsolutions/openmrs-module-initializer) applies when the server starts.

Billing configuration lives in your content package under `configuration/backend_configuration/`:

```
configuration/backend_configuration/
├── globalproperties/billing.xml
├── billableservices/billableServices.csv
├── paymentmodes/paymentModes.csv
├── cashpoints/cashPoints.csv
└── cashieritemprices/cashierItemPrices.csv
```

**Please make sure that the folder names are named correctly.**

#### Global properties

- `billing.defaultReceiptReportId`: Jasper report ID for receipt generation
- `billing.defaultShiftReportId`: Jasper report ID for shift reports
- `billing.receipt.logoPath`: Path to receipt logo image
- `billing.systemReceiptNumberGenerator`: Class name for receipt number generator (default: `org.openmrs.module.billing.api.SequentialReceiptNumberGenerator`)
- `billing.sequenceBlockSize`: Number of receipt sequence values reserved per database round-trip (default: 100). Larger blocks reduce database contention; smaller blocks reduce the sequence values skipped on restart (up to blockSize - 1 per group). Receipt numbers are always unique but may skip values.
  Every `billing.*` property which you can find defined in the `config.xml` file in this repository inside a `<globalProperty>` tag, goes in `globalproperties/billing.xml`

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

#### Billable services

Services a facility can charge for. `Concept` and `Service Type` are references to existing concepts — by UUID, name,
or a `source:code` mapping.

```csv
Uuid, Void/Retire, Service Name, Short Name, Concept, Service Type, Service Status
44ebd6cd-04ad-4eba-8ce1-0de4564bfd17,, Antenatal care, ANTC, 1592AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, Antenatal Services, Enabled
360fab13-d92b-4a9f-ad4e-0ac223e7f54c,, OPD consultation, OPDC, 160542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, Outpatient Services, Enabled
```

`Service Status` defaults to `Enabled` when left blank.

#### Payment modes

The forms of payment a cashier can accept. `attributes` is optional and defines the extra fields captured with a
payment: a semicolon-separated list, each entry split by `::` into `name`, `format`, `regex` and `required`.

```csv
Uuid, Void/Retire, name, attributes
526bf278-ba81-4436-b867-c2f6641d060a,, Cash,
2b1b9aae-5d35-43dd-9214-3fd370fd7737,, Bank transfer,
e168c141-f5fd-4eec-bd3e-633bed1c9606,, Mobile money, Phone Number::Text::::True;Reference
```

#### Cash points

The cashier stations bills are raised at. `location` references an existing location, so define your locations in the
same content package.

```csv
Uuid, Void/Retire, name, description, location
54065383-b4d4-42d2-af4d-d250a1fd2590,, OPD Cash Point, OPD cash point for billing, Opd Clinic
ba685651-ed3b-4e63-9b35-78893060758a,, IPD Cash Point, IPD cash point for billing, Inpatient Ward
```

#### Item prices

What each service or stock item costs under a given payment mode. Set **exactly one** of `Stock Item` or
`Billable Service` per row — rows with both, or neither, are rejected.

```csv
Uuid, Void/Retire, Name, Price, Payment Mode, Stock Item, Billable Service
c1c1c1c1-0000-0000-0000-000000000001,, ANC Service Price, 150.00, 526bf278-ba81-4436-b867-c2f6641d060a,, 44ebd6cd-04ad-4eba-8ce1-0de4564bfd17
c1c1c1c1-0000-0000-0000-000000000004,, Paracetamol Item Price, 20.00, 526bf278-ba81-4436-b867-c2f6641d060a, b2b2b2b2-0000-0000-0000-000000000001,
```

`Payment Mode`, `Stock Item` and `Billable Service` are all matched by UUID.

For the full header-by-header documentation of each domain, see the Initializer docs for
[billableservices](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/billableservices.md),
[paymentmodes](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/paymentmodes.md),
[cashpoints](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/cashpoints.md) and
[cashieritemprices](https://github.com/mekomsolutions/openmrs-module-initializer/blob/main/readme/cashieritemprices.md).

### Assign privileges

The module defines granular privileges for bill management (view, manage, adjust, purge, refund, reprint), discount
management (view, manage, approve), refund management (view, request, approve, complete), metadata management (view,
manage, purge), timesheet management (view, manage, purge), and app access for OpenMRS 2.x (cashier app, tasks,
reports). See `omod/src/main/resources/config.xml` for the complete list.

Assign them to roles through the Initializer `roles` and `privileges` domains so role setup ships with the rest of your
configuration.

## Global properties reference

**Receipts and reports**

| Property                               | Default                                                           | Description                                                                                                 |
| -------------------------------------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `billing.defaultReceiptReportId`       | —                                                                 | ID of the Jasper report used to generate a receipt on the Bill page                                         |
| `billing.defaultShiftReportId`         | —                                                                 | ID of the Jasper cashier shift report                                                                       |
| `billing.receipt.logoPath`             | —                                                                 | Path to the logo image printed on receipts                                                                  |
| `billing.currencySymbol`               | —                                                                 | Currency shown on receipts (e.g. `USD`, `KES`, or custom text). Falls back to the locale default when unset |
| `billing.systemReceiptNumberGenerator` | `org.openmrs.module.billing.api.SequentialReceiptNumberGenerator` | Fully-qualified class name of the receipt number generator                                                  |
| `billing.sequenceBlockSize`            | `100`                                                             | Receipt sequence values reserved per database round-trip. Must be at least 1                                |

**Bill rounding**

| Property                 | Default | Description                                              |
| ------------------------ | ------- | -------------------------------------------------------- |
| `billing.roundingMode`   | —       | How bill totals are rounded: `FLOOR`, `MID` or `CEILING` |
| `billing.roundToNearest` | —       | Nearest unit to round to. May be a decimal               |
| `billing.roundingItemId` | —       | ID of the item used to account for bill total rounding   |
| `billing.roundingDeptId` | —       | ID of the department the rounding item belongs to        |

**Bill behaviour**

| Property                               | Default | Description                                                                                               |
| -------------------------------------- | ------- | --------------------------------------------------------------------------------------------------------- |
| `billing.timesheetRequired`            | —       | Require an active timesheet before a bill can be created                                                  |
| `billing.allowBillAdjustments`         | `true`  | Enable bill adjustments                                                                                   |
| `billing.adjustmentReasonField`        | —       | Require a reason when adjusting a bill                                                                    |
| `billing.autofillPaymentAmount`        | `false` | Pre-fill the payment amount with the remaining balance                                                    |
| `billing.discountEnabled`              | `true`  | Enable bill discount management                                                                           |
| `billing.refundEnabled`                | `true`  | Enable refund requests and approval                                                                       |
| `billing.patientDashboard2BillCount`   | `5`     | Bills shown on the OpenMRS 2.x patient dashboard. Falls back to 4 if the property is blank or non-numeric |
| `billing.patientPaymentStatusResolver` | —       | Fully-qualified class name of the patient payment status resolver. Blank uses the built-in one            |

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
`blockSize - 1` values per sequence group. Larger blocks reduce contention under load; smaller blocks reduce the gaps.

To use your own numbering scheme, implement `org.openmrs.module.billing.api.IReceiptNumberGenerator` in another module
and set `billing.systemReceiptNumberGenerator` to its fully-qualified class name.

### Patient payment status

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
