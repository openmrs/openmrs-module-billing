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

### QueryStore / Chart Search Integration

Projects billing records into the [OpenMRS QueryStore](https://github.com/openmrs/openmrs-module-querystore) so a clinician-facing semantic / keyword chart search (e.g. `chartsearchai`) can retrieve them. Three patient-scoped resource types are contributed through QueryStore's `ResourceTypeProvider` SPI:

- **`billing_bill`** — one document per bill; its searchable text folds in the billed services / drugs / tests (line items), the raw total, amount paid, payment status and cash point. (Discount-adjusted figures are intentionally left to `billing_discount`, since approving a discount does not re-save the bill and a folded balance would go stale.)
- **`billing_discount`** — fee waivers / discounts (often a signal of a subsidized programme or financial hardship).
- **`billing_refund`** — refunds.

Steady-state indexing is event-driven and needs **no** changes to the base billing services: QueryStore's events-first consumer projects any entity for which a serializer is registered, using the `*ServiceEvent`s that core [openmrs-core#6084](https://github.com/openmrs/openmrs-core/pull/6084) publishes for `save*` / `void*` / `purge*` service methods — which billing's `saveBill` / `voidBill` / `purgeBill` (and `saveBillDiscount` / `saveBillRefund`) already are. Backfill of pre-existing records is admin-triggered via QueryStore's reindex endpoint (`POST /ws/rest/v1/querystore/reindex {"scope":"all"}`) or `BootstrapService`.

Because QueryStore and the core `*ServiceEvent` API require **OpenMRS Platform 2.9** plus its Elasticsearch / Lucene backend, this integration is packaged as a **separate, optional `billing-querystore` omod** and is **not** part of the default build — the base billing module is unaffected and still targets 2.7.8. Build it explicitly and install the resulting `querystore/target/billing-querystore-*.omod` alongside billing only on servers that run QueryStore:

```bash
mvn -Pquerystore clean install
```

> **Runtime note:** the QueryStore SPI documents that whether a *module's* services emit the core `*ServiceEvent`s is deployment-dependent and should be verified on a live 2.9 server. If bills backfill but do not live-update, billing's services can publish the events themselves via a small aspect added to this submodule; backfill works regardless.

## Requirements

- **OpenMRS Platform**: 2.7.8 (built and tested against; module `require_version` follows the build property)
- **Java Version**: 1.8 or higher
- **Required Modules**:
  - Web Services REST Module 2.9+
  - Stock Management Module 1.4.0+
- **Optional Modules**:
  - FHIR2 Module 2.4.0+ (required to use the FHIR Invoice submodule)
  - QueryStore Module 1.0.0+ on Platform 2.9+ (required to use the optional `billing-querystore` chart-search integration)
  - IDGen Module 2.8+ (for custom receipt number generation)
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

### Global Properties

The module provides several global properties for configuration:

**Receipt and Report Configuration**:

- `billing.defaultReceiptReportId`: Jasper report ID for receipt generation
- `billing.defaultShiftReportId`: Jasper report ID for shift reports
- `billing.receipt.logoPath`: Path to receipt logo image
- `billing.systemReceiptNumberGenerator`: Class name for receipt number generator (default: `org.openmrs.module.billing.api.SequentialReceiptNumberGenerator`)

**Bill Rounding**:

- `billing.roundingMode`: Bill total rounding mode (FLOOR, MID, CEILING)
- `billing.roundToNearest`: Nearest unit to round to (decimal number)
- `billing.roundingItemId`: ID of the item used to account for bill total rounding
- `billing.roundingDeptId`: ID of the department of the rounding item

**Bill Behavior**:

- `billing.timesheetRequired`: Require active timesheet for bill creation (true/false)
- `billing.allowBillAdjustments`: Enable/disable bill adjustments (default: true)
- `billing.adjustmentReasonField`: Require adjustment reason field (true/false)
- `billing.autofillPaymentAmount`: Auto-fill payment amount with remaining balance (default: false)
- `billing.discountEnabled`: Enable bill discount management (default: true)
- `billing.refundEnabled`: Enable refund requests and approval (default: true)
- `billing.patientPaymentStatusResolver`: Fully-qualified class name of the patient payment status resolver
- `billing.patientDashboard2BillCount`: Number of bills to show on patient dashboard (default: 4)

**Financial Reports**:

- `billing.reports.departmentCollections`: ID of the Department Collections report
- `billing.reports.departmentRevenue`: ID of the Department Revenue report
- `billing.reports.shiftSummary`: ID of the Shift Summary report
- `billing.reports.dailyShiftSummary`: ID of the Daily Shift Summary report
- `billing.reports.paymentsByPaymentMode`: ID of the Payments by Payment Mode report

### Privileges

The module defines granular privileges for bill management (view, manage, adjust, purge, refund, reprint), discount management (view, manage, approve), refund management (view, request, approve, complete), metadata management (view, manage, purge), timesheet management (view, manage, purge), and app access for OpenMRS 2.x (cashier app, tasks, reports). See `omod/src/main/resources/config.xml` for the complete list.

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
