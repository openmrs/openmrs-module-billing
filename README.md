# OpenMRS Billing Module

The OpenMRS Billing Module is a comprehensive billing and payment management system for OpenMRS. It provides a complete solution for healthcare facilities to manage patient billing, process payments, generate receipts, and track financial transactions.

## Key Features

### Bill Management

Create and manage patient bills with multiple line items, track bills through various states (draft, posted, adjusted), and handle bill adjustments with full audit trail and refunds with proper authorization.

### Payment Processing

Process payments using multiple payment modes (cash, insurance, mobile money, credit/debit cards, custom modes) with support for partial payments, payment attributes, and automatic change calculation.

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

Automatically generate bills from clinical orders with order-to-bill line item mapping.

### REST API & Integration

Provides REST API endpoints at `/rest/v1/billing/*` for bills, payments, payment modes, billable services, cash points, timesheets, and item prices. Includes patient dashboard integration for OpenMRS 2.x with configurable bill history widget. Supports English, French, and Spanish translations.

## Requirements

- **OpenMRS Version**: 2.4.0 or higher
- **Java Version**: 1.8 or higher
- **Required Modules**:
  - Web Services REST Module 2.9+
  - Stock Management Module 1.4.0+
- **Optional Modules**:
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
- `billing.patientDashboard2BillCount`: Number of bills to show on patient dashboard (default: 5)

**Financial Reports**:

- `billing.reports.departmentCollections`: ID of the Department Collections report
- `billing.reports.departmentRevenue`: ID of the Department Revenue report
- `billing.reports.shiftSummary`: ID of the Shift Summary report
- `billing.reports.dailyShiftSummary`: ID of the Daily Shift Summary report
- `billing.reports.paymentsByPaymentMode`: ID of the Payments by Payment Mode report

### Privileges

The module defines granular privileges for bill management (view, manage, adjust, purge, refund, reprint), metadata management (view, manage, purge), timesheet management (view, manage, purge), and app access for OpenMRS 2.x (cashier app, tasks, reports). See `omod/src/main/resources/config.xml` for the complete list.

## Documentation

- **User Documentation**: [OpenMRS Billing Module Wiki](https://openmrs.atlassian.net/wiki/x/XIeEAQ)

## Repository Information

This repository follows the [git flow](https://github.com/nvie/gitflow) branching model:

- **main**: Current released version
- **develop**: Current development version
- **Tags**: Each release is tagged with version number (e.g., v1.3.2)

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
2. Create a feature branch from `develop`
3. Make your changes following OpenMRS coding conventions
4. Write tests for new functionality
5. Submit a pull request to the `develop` branch

## License

This module is licensed under the OpenMRS Public License. See [LICENSE.txt](LICENSE.txt) for details.

## Support

For questions, feedback, or issues:

- Post in the #openmrs-billing channel in the OpenMRS Slack community
- Post on the [OpenMRS Talk](https://talk.openmrs.org/) community forum
- Create an issue in this repository

---
