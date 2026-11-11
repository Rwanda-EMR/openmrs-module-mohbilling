# RHIP Voucher Integration Notes

## Overview
This document summarizes how RHIP voucher creation is integrated in mohbilling, based on the following components:

- `omod/src/main/webapp/mohBillingViewGlobalBill.jsp`
- `omod/src/main/java/org/openmrs/module/mohbilling/web/controller/MohBillingViewGlobalBillController.java`
- `api/src/main/resources/InsurancePolicy.hbm.xml`
- `api/src/main/resources/liquibase.xml`
- `omod/src/main/java/org/openmrs/module/mohbilling/rest/InsuranceVoucherRestController.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/IntegrationResponse.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/insurance/RhipVoucherService.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/insurance/RhipVoucherRequest.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/insurance/RhipVoucherProvider.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/insurance/RhipVoucherProcedure.java`
- `api/src/main/java/org/openmrs/module/mohbilling/integration/insurance/RhipVoucherIntegrationConfig.java`

## UI Flow (Global Bill Screen)
- Page: `mohBillingViewGlobalBill.jsp`
- The "Send RHIP Voucher" button appears only when:
  - The global bill is closed, and
  - The insurance category is `MUTUELLE`.
- Clicking the button shows a confirmation dialog via `confirm(...)` and submits a hidden form.
- After a successful submission, the page shows a "RHIP Voucher Receipt" box with:
  - Voucher Code
  - Reference Number
  - Status
  These values come from session attributes set in the controller and are cleared after display.

## Controller Flow (Global Bill Controller)
- Controller: `MohBillingViewGlobalBillController`
- When `send_voucher` is posted:
  - The controller calls `RhipVoucherService.submitVoucherForGlobalBill(...)`.
  - It checks for disabled integration, missing response, or errors and sets an error message when applicable.
  - It processes RHIP response content to:
    - Log response details
    - Populate voucher receipt data in session
    - Show a success or error message
- Response parsing is tolerant of either a Map or JSON string body.

## REST Endpoint (Optional/External Use)
- Controller: `InsuranceVoucherRestController`
- Endpoint: `POST /rest/v1/mohbilling/insurance/voucher`
- Accepts a JSON `RhipVoucherRequest` payload.
- Returns a simplified response Map to avoid Jackson recursion issues:
  - `enabled`
  - `endpointAccessible`
  - `responseCode`
  - `errorMessage`
  - `responseBody` (string)

## Integration Service Layer

### RhipVoucherService
- Builds the `RhipVoucherRequest` payload from the global bill.
- Determines:
  - Insurance type (CBHI/MUTUELLE)
  - FOSA ID (from global property)
  - Patient identifier (policy or insurance card)
  - Procedures, practitioner details, and dates
- Phone number is read from a person attribute type configured in global properties.

### RhipVoucherProvider
- Sends the request to RHIP using HTTP POST.
- Builds JSON manually to avoid Jackson version conflicts.
- Reads the raw response body as a string and returns it in `IntegrationResponse`.

### RhipVoucherRequest
- DTO used for RHIP request payload.
- Contains insurance type, facility FOSA ID, patient identifier, procedures, provider info, dates, and phone number.

### RhipVoucherProcedure
- DTO for individual procedure entries in the RHIP payload.

### RhipVoucherIntegrationConfig
- Reads global properties for integration settings:
  - `mohbilling.rhipVoucher.url`
  - `mohbilling.rhipVoucher.apiKey`
  - `mohbilling.rhipVoucher.apiOrigin`
  - `mohbilling.rhipVoucher.providerLicenseAttributeTypeUuid`
  - `mohbilling.rhipVoucher.patientIdentifierAttributeTypeUuid`
  - `mohbilling.rhipVoucher.diagnosisConceptIds`
  - `mohbilling.rhipVoucher.defaultFosaId`
  - `mohbilling.rhipVoucher.defaultPatientType`
  - `mohbilling.rhipVoucher.defaultHealthCareStayType`
  - `mohbilling.rhipVoucher.defaultTreatmentForNewBorn`
  - `mohbilling.rhipVoucher.patientPhoneAttributeTypeUuid`

## IntegrationResponse
- Generic response wrapper used across integration calls.
- Holds:
  - `enabled` flag
  - `endpointAccessible`
  - `responseCode`
  - `responseEntity` (raw body)
  - `errorMessage`

## Mapping / Persistence
- `InsurancePolicy.hbm.xml` and `liquibase.xml` support the insurance policy data needed for vouchers (e.g., policy data and RHIP fields like patient identifiers). No eligibility tables are required for voucher submission.

## Notes
- Eligibility checks are removed in mohbilling; voucher creation does not call eligibility endpoints.
- RHIP response details are logged and surfaced to the UI on success.
- The Send Voucher UI is limited to `MUTUELLE` insurance category and closed bills.
