# MOH Billing REST API Endpoints Documentation

## Base URL
All endpoints are prefixed with: `/ws/rest/v1/mohbilling/`

---

## 1. Admission Resource

**Endpoint:** `/ws/rest/v1/mohbilling/admission`

### Operations
- **GET** `/{id}` - Get admission by ID
- **POST** `/` - Create new admission

### Create/Update Payload
```json
{
  "insurancePolicy": {
    "uuid": "insurancePolicyId"
  },
  "admissionDate": "2025-10-15T10:30:00.000+0000",
  "admissionType": "INPATIENT"
}
```


**Required Properties:**
- `insurancePolicy` (object with ID)
- `admissionDate` (date)
- `admissionType` (string)

**DELETE:** ❌ Not supported

---

## 2. Department Resource

**Endpoint:** `/ws/rest/v1/mohbilling/department`

### Operations
- **GET** `/{id}` - Get department by ID
- **POST** `/` - Create new department
- **GET** `/` - List all departments

### Create/Update Payload
```json
{
  "name": "Emergency Department",
  "description": "Emergency services"
}
```


**Required Properties:**
- `name` (string)
- `description` (string)

**DELETE:** ❌ Not supported

---

## 3. Insurance Resource

**Endpoint:** `/ws/rest/v1/mohbilling/insurance`

### Operations
- **GET** `/{id}` - Get insurance by ID
- **POST** `/` - Create new insurance
- **GET** `/` - List all insurances
- **GET** `/?includeAll=true` - List all including voided

### Create/Update Payload
```json
{
  "name": "National Health Insurance",
  "address": "123 Main St",
  "phone": "+250123456789",
  "concept": {
    "uuid": "conceptId"
  },
  "rates": [
    {
      "rate": 0.85,
      "flatFee": "1000",
      "startDate": "2025-01-01T00:00:00.000+0000",
      "endDate": null
    }
  ],
  "categories": []
}
```


**Properties:**
- `name` (string)
- `address` (string)
- `phone` (string)
- `concept` (object with ID)
- `rates` (array of rate objects)
- `categories` (array)

**DELETE:** ❌ Not supported

---

## 4. Insurance Policy Resource

**Endpoint:** `/ws/rest/v1/mohbilling/insurancePolicy`

### Operations

#### **GET by ID**
```
GET /insurancePolicy/{id}
```

Retrieves a specific insurance policy by its ID.

#### **GET All (Paginated)**
```
GET /insurancePolicy?startIndex=0&limit=20
```

Retrieves all insurance policies with pagination support.

**Query Parameters:**
- `startIndex` (optional) - Starting index for pagination (default: 0)
- `limit` (optional) - Number of records to return (default: 10)

#### **Search**
```
GET /insurancePolicy?patientId=123
GET /insurancePolicy?patient=patientUuid
GET /insurancePolicy?insuranceCardNo=RAMA-2024-001234
```


**Search Parameters:**
- `patientId` - Search by patient internal ID
- `patient` - Search by patient UUID
- `insuranceCardNo` - Search by insurance card number

#### **POST (Create)**
```
POST /insurancePolicy
```


**Create Payload:**
```json
{
  "insurance": {
    "uuid": "insuranceId"
  },
  "owner": {
    "uuid": "patientUuid"
  },
  "insuranceCardNo": "RAMA-2024-001234",
  "coverageStartDate": "2024-01-01T00:00:00.000+0000",
  "expirationDate": "2024-12-31T23:59:59.000+0000",
  "thirdParty": {
    "uuid": "thirdPartyId"
  },
  "beneficiaries": [
    {
      "patient": {
        "uuid": "beneficiaryPatientUuid"
      },
      "policyIdNumber": "RAMA-2024-001234"
    }
  ]
}
```


**Properties:**
- `insurance` (object) - Insurance company reference
- `owner` (object) - Patient who owns the policy
- `insuranceCardNo` (string) - Insurance card/policy number
- `coverageStartDate` (date) - Coverage start date
- `expirationDate` (date, optional) - Coverage expiration date
- `thirdParty` (object, optional) - Third party payer
- `beneficiaries` (array, optional) - List of beneficiaries

#### **POST (Update)**
```
POST /insurancePolicy/{id}
```

Updates an existing insurance policy. Use the same payload structure as create.

#### **DELETE (Soft Delete)**
```
DELETE /insurancePolicy/{id}
```

✅ **Supported** - Performs a soft delete by marking the policy as retired.

**Note:** This operation sets:
- `retired` = true
- `retiredBy` = authenticated user
- `retiredDate` = current date
- `retireReason` = "Deleted via REST"

---

## 5. Insurance Rate Resource

**Endpoint:** `/ws/rest/v1/mohbilling/insuranceRate`

### Operations
- **GET** `/` - List all insurance rates

### Payload Structure (Read-Only)
```json
{
  "insurance": {
    "uuid": "insuranceId"
  },
  "rate": 0.85,
  "flatFee": "1000.00",
  "startDate": "2025-01-01T00:00:00.000+0000",
  "endDate": "2025-12-31T23:59:59.000+0000"
}
```


**CREATE/UPDATE/DELETE:** ❌ Not supported

*Note: Rates are managed through the Insurance resource.*

---

## 6. Facility Service Price Resource

**Endpoint:** `/ws/rest/v1/mohbilling/facilityServicePrice`

### Operations
- **GET** `/{id}` - Get facility service price by ID
- **POST** `/` - Create new facility service price
- **GET** `/` - List all facility service prices

### Create/Update Payload
```json
{
  "name": "X-Ray Chest",
  "shortName": "XRAY-CHEST",
  "description": "Chest X-Ray service",
  "fullPrice": "5000.00",
  "startDate": "2025-01-01T00:00:00.000+0000",
  "location": {
    "uuid": "locationId"
  },
  "category": "RADIOLOGY",
  "endDate": null,
  "concept": {
    "uuid": "conceptId"
  }
}
```


**Required Properties:**
- `name` (string)
- `fullPrice` (decimal/string)
- `startDate` (date)
- `location` (object with ID)

**Optional Properties:**
- `shortName` (string)
- `description` (string)
- `category` (string)
- `endDate` (date)
- `concept` (object with ID)

**DELETE:** ❌ Not supported

---

## 7. Hop Service Resource

**Endpoint:** `/ws/rest/v1/mohbilling/hopService`

### Operations
- **GET** `/{id}` - Get hop service by ID
- **POST** `/` - Create new hop service
- **GET** `/` - List all hop services
- **GET** `/?department={id}` - Search hop services by department

### Create/Update Payload
```json
{
  "name": "Surgery Service",
  "description": "Surgical procedures and operations"
}
```


**Required Properties:**
- `name` (string) - Service name
- `description` (string) - Service description

**Search Parameters:**
- `department` - Filter by department ID

**DELETE:** ❌ Not supported

---

## 8. Service Category Resource

**Endpoint:** `/ws/rest/v1/mohbilling/serviceCategory`

### Operations
- **GET** `/{id}` - Get service category by ID
- **GET** `/` - List all service categories
- **GET** `/?departmentId={id}&ipCardNumber={number}` - Search by department and insurance policy

### Response Properties
- `serviceCategoryId` (integer) - Unique identifier
- `name` (string) - Category name
- `department` (object) - Associated department
- `hopService` (object) - Associated hop service
- `description` (string) - Category description
- `price` (decimal) - Category price

**Search Parameters:**
- `departmentId` - Filter by department ID
- `ipCardNumber` - Filter by insurance card number

**CREATE/UPDATE/DELETE:** ❌ Not supported (Read-only resource)

---

## 9. Third Party Resource

**Endpoint:** `/ws/rest/v1/mohbilling/thirdParty`

### Operations
- **GET** `/{id}` - Get third party by ID
- **POST** `/` - Create new third party
- **GET** `/` - List all third parties

### Create/Update Payload
```json
{
  "name": "Employer Co-Payment",
  "rate": 0.10
}
```


**Required Properties:**
- `name` (string) - Third party name
- `rate` (float) - Coverage rate (0.0 to 1.0)

**Response Properties:**
- `thirdPartyId` (integer)
- `name` (string)
- `rate` (float)

**DELETE:** ❌ Not supported

---

## 10. Third Party Bill Resource

**Endpoint:** `/ws/rest/v1/mohbilling/thirdPartyBill`

### Operations
- **POST** `/` - Create new third party bill

### Response Properties
- `amount` (decimal)
- `creator` (user object)
- `createdDate` (date)

**GET/DELETE:** ❌ Not supported

*Note: Third party bills are typically auto-generated when creating consommations.*

---

## 11. Consommation Resource

**Endpoint:** `/ws/rest/v1/mohbilling/consommation`

### Operations
- **GET** `/{id}` - Get consommation by ID
- **POST** `/` - Create new consommation
- **GET** `/?globalBillId={id}` - Get consommations by global bill
- **GET** `/?patientName={name}&policyIdNumber={number}` - Search by patient or policy

### Create/Update Payload
```json
{
  "department": {
    "uuid": "departmentId"
  },
  "beneficiary": {
    "uuid": "beneficiaryId"
  },
  "globalBill": {
    "uuid": "globalBillId"
  },
  "billItems": [
    {
      "facilityServicePrice": {
        "uuid": "servicePriceId"
      },
      "unitPrice": "5000.00",
      "quantity": "2"
    }
  ]
}
```


**Required Properties:**
- `department` (object with ID)
- `beneficiary` (object with ID)
- `globalBill` (object with ID)

**Optional Properties:**
- `billItems` (array of bill item objects)

**DELETE:** ❌ Not supported

---

## 12. Global Bill Resource

**Endpoint:** `/ws/rest/v1/mohbilling/globalBill`

### Operations
- **GET** `/{id}` - Get global bill by ID
- **POST** `/` - Create new global bill
- **GET** `/` - Search/list global bills with various parameters

---

## 13. Patient Bill Resource

**Endpoint:** `/ws/rest/v1/mohbilling/patientBill`

### Operations
- **GET** `/{id}` - Get patient bill by ID
- **POST** `/` - Create new patient bill
- **DELETE** `/{id}` - Void patient bill (soft delete)

**DELETE:** ✅ Supported - Voids the patient bill (soft delete)

---

## 14. Insurance Bill Resource

**Endpoint:** `/ws/rest/v1/mohbilling/insuranceBill`

### Operations
- **GET** `/{id}` - Get insurance bill by ID
- **POST** `/` - Create new insurance bill

---

## 15. Bill Payment Resource

**Endpoint:** `/ws/rest/v1/mohbilling/billPayment`

### Operations
- **GET** `/{id}` - Get bill payment by ID
- **POST** `/` - Create new bill payment
- **GET** `/` - List all bill payments
- **GET** `/?patientBill={id}` - Get payments for a specific patient bill

### Create Payload
```json
{
  "amountPaid": 5000.00,
  "patientBill": {
    "uuid": "patientBillId"
  },
  "dateReceived": "2025-10-15T10:30:00.000+0000",
  "collector": {
    "uuid": "userId"
  },
  "paidItems": []
}
```


**Required Properties:**
- `amountPaid` (decimal) - Amount paid
- `patientBill` (object with ID) - Associated patient bill

**Optional Properties:**
- `dateReceived` (date) - Payment date
- `collector` (object with ID) - User who collected payment
- `paidItems` (array) - Specific items paid

**DELETE:** ❌ Not supported

---

## 16. Billable Service Resource

**Endpoint:** `/ws/rest/v1/mohbilling/billableService`

### Operations
- **GET** `/{id}` - Get billable service by ID
- **GET** `/` - List all billable services
- **GET** `/?serviceCategoryId={id}` - Search by service category
- **GET** `/?facilityServicePriceId={id}` - Search by facility service price
- **GET** `/?serviceCategoryId={id}&facilityServicePriceId={id}` - Search by both

### Response Properties
- `serviceId` (integer)
- `insurance` (object)
- `maximaToPay` (decimal)
- `facilityServicePrice` (object)
- `startDate` (date)
- `endDate` (date)

**CREATE/UPDATE/DELETE:** ❌ Not supported (Read-only resource)

---

## 17. Additional Resources

The following resources also exist in the module:
- **BeneficiaryResource** - `/ws/rest/v1/mohbilling/beneficiary`
- **PaidServiceBillResource** - `/ws/rest/v1/mohbilling/paidServiceBill`
- **PatientServiceBillResource** - `/ws/rest/v1/mohbilling/patientServiceBill`

---

## General Notes

### Object References
When a property is an object (not a primitive), pass it as:
```json
{"uuid": "entityId"}
```


### Dates
Use ISO 8601 format:
```
"2025-10-15T10:30:00.000+0000"
```


### Authentication
All endpoints require OpenMRS authentication

### Supported Versions
OpenMRS 2.0 - 2.*

### Representations
Use `?v=ref`, `?v=default`, or `?v=full` to control response detail level

---

## DELETE Operation Support Summary

| Resource | DELETE Supported |
|----------|------------------|
| Admission | ❌ No |
| Department | ❌ No |
| Insurance | ❌ No |
| **Insurance Policy** | **✅ Yes (Soft Delete)** |
| Insurance Rate | ❌ No |
| Facility Service Price | ❌ No |
| Hop Service | ❌ No |
| Service Category | ❌ No |
| Third Party | ❌ No |
| Third Party Bill | ❌ No |
| Consommation | ❌ No |
| **Patient Bill** | **✅ Yes (Void)** |
| Bill Payment | ❌ No |
| Billable Service | ❌ No |