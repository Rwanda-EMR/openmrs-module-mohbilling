package org.openmrs.module.mohbilling.irembo;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.module.mohbilling.irembo.models.Customer;
import org.openmrs.module.mohbilling.irembo.models.PaymentItem;
import org.openmrs.module.mohbilling.irembo.util.DateToJson;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.IremboPayResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice extends Config {
    private float amount;
    private String invoiceNumber;
    private String transactionId;
    private Date createdAt;
    private Date updatedAt;
    private Date expiryAt;
    private String description;
    private String paymentLinkUrl;
    private String type;
    private String paymentStatus;
    private String currency;
    private Customer customer;
    private String createdBy;
    private String paymentAccountIdentifier;
    private List<PaymentItem> paymentItems;
    private String language;

    public Invoice(String apiKey, Environment environment) {
        super(apiKey, environment);
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentLinkUrl() {
        return paymentLinkUrl;
    }

    public void setPaymentLinkUrl(String paymentLinkUrl) {
        this.paymentLinkUrl = paymentLinkUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getPaymentAccountIdentifier() {
        return paymentAccountIdentifier;
    }

    public void setPaymentAccountIdentifier(String paymentAccountIdentifier) {
        this.paymentAccountIdentifier = paymentAccountIdentifier;
    }

    public List<PaymentItem> getPaymentItems() {
        return paymentItems;
    }

    public void setPaymentItems(List<PaymentItem> paymentItems) {
        this.paymentItems = paymentItems;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(Date expiryAt) {
        this.expiryAt = expiryAt;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "amount=" + amount +
                ", invoiceNumber=" + invoiceNumber +
                ", transactionId='" + transactionId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", expiryAt=" + expiryAt +
                ", description='" + description + '\'' +
                ", paymentLinkUrl='" + paymentLinkUrl + '\'' +
                ", type='" + type + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", currency='" + currency + '\'' +
                ", customer=" + customer +
                ", createdBy='" + createdBy + '\'' +
                ", paymentAccountIdentifier='" + paymentAccountIdentifier + '\'' +
                ", paymentItems=" + paymentItems +
                ", language='" + language + '\'' +
                '}';
    }

    public IremboPayResponse<Invoice> createInvoice(String transactionId, String paymentAccountIdentifier, Customer customer, List<PaymentItem> paymentItems, String description, Date expiryAt, String language) {
        String url = baseUrl + "/invoices";
        // Create JSON payload
        JSONObject payload = new JSONObject();
        payload.put("transactionId", transactionId);
        payload.put("paymentAccountIdentifier", paymentAccountIdentifier);

        JSONObject customerJson = new JSONObject();
        customerJson.put("email", customer.getEmail());
        customerJson.put("phoneNumber", customer.getPhoneNumber());
        customerJson.put("name", customer.getFullName());
        payload.put("customer", customerJson);


        for (PaymentItem item : paymentItems) {
            JSONObject paymentItem = new JSONObject();
            paymentItem.put("quantity", item.getQuantity());
            paymentItem.put("unitAmount", item.getUnitAmount());
            paymentItem.put("code", item.getCode());
            payload.append("paymentItems", paymentItem);
        }

        payload.put("description", description);
        payload.put("expiryAt", new DateToJson(expiryAt).serialize());
        payload.put("language", language);
        OkHttpClient client = new OkHttpClient();
        // Create request body
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), payload.toString());

        // Create request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("irembopay-secretKey", apiKey)
                .addHeader("User-Agent", "IremboPaySDK")
                .build();
        // Send request
        try {
            Response response = client.newCall(request).execute();
            IremboPayResponse<Invoice> iremboPayResponse = new IremboPayResponse<>();
            assert response.body() != null;
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            iremboPayResponse.setMessage(responseJsonObject.getString("message"));
            iremboPayResponse.setSuccess(responseJsonObject.getBoolean("success"));

            if (iremboPayResponse.success) {
                iremboPayResponse.setData(getInstanceFromJson(responseJsonObject));
            } else {
                for (Object errorObject : responseJsonObject.getJSONArray("errors")) {
                    JSONObject error = (JSONObject) errorObject;
                    iremboPayResponse.addError(new IremboPayResponse.Error(error.getString("code"), error.getString("detail")));
                }

            }
            return iremboPayResponse;
        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
            return null;
        }
    }

    public IremboPayResponse<Invoice> createBatchInvoice(List<String> invoiceNumbers, String transactionId, String description) {
        String url = baseUrl + "/invoices/batch";
        JSONObject payload = new JSONObject();
        JSONArray invoiceNumberArray = new JSONArray();
        for (String invoiceNumber : invoiceNumbers) {
            invoiceNumberArray.put(invoiceNumber);
        }
        payload.put("invoiceNumbers", invoiceNumberArray);
        payload.put("transactionId", transactionId);
        payload.put("description", description);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), payload.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "IremboPaySDK")
                .addHeader("irembopay-secretKey", apiKey)
                .build();
        // Send request
        try {
            Response response = client.newCall(request).execute();
            IremboPayResponse<Invoice> iremboPayResponse = new IremboPayResponse<>();
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            iremboPayResponse.setMessage(responseJsonObject.getString("message"));
            iremboPayResponse.setSuccess(responseJsonObject.getBoolean("success"));
            if (iremboPayResponse.success) {
                iremboPayResponse.setData(getInstanceFromJson(responseJsonObject));
            } else {
                for (Object errorObject : responseJsonObject.getJSONArray("errors")) {
                    JSONObject error = (JSONObject) errorObject;
                    iremboPayResponse.addError(new IremboPayResponse.Error(error.getString("code"), error.getString("detail")));
                }

            }
            return iremboPayResponse;
        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
            return null;
        }


    }

    public IremboPayResponse<Invoice> getInvoice(String invoiceReference) {
        OkHttpClient client = new OkHttpClient();
        String url = baseUrl + "/invoices/" + invoiceReference;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("irembopay-secretKey", apiKey)
                .addHeader("User-Agent", "IremboPaySDK")
                .build();

        try {
            Response response = client.newCall(request).execute();
            IremboPayResponse<Invoice> iremboPayResponse = new IremboPayResponse<>();
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            iremboPayResponse.setMessage(responseJsonObject.getString("message"));
            iremboPayResponse.setSuccess(responseJsonObject.getBoolean("success"));
            if (iremboPayResponse.success) {
                iremboPayResponse.setData(getInstanceFromJson(responseJsonObject));
            } else {
                for (Object errorObject : responseJsonObject.getJSONArray("errors")) {
                    JSONObject error = (JSONObject) errorObject;
                    iremboPayResponse.addError(new IremboPayResponse.Error(error.getString("code"), error.getString("detail")));
                }
            }
            return iremboPayResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public IremboPayResponse<Invoice> updateInvoice(String invoiceNumber, Date expiry, List<PaymentItem> paymentItems) throws IOException {
        String url = baseUrl + "/invoices/" + invoiceNumber;
        // Create JSON payload
        JSONObject payload = new JSONObject();
        payload.put("expiryAt", new DateToJson(expiry).serialize());
        JSONArray paymentItemsArray = new JSONArray();

        for (PaymentItem paymentItem : paymentItems) {
            JSONObject paymentItemObject = new JSONObject();
            paymentItemObject.put("unitAmount", paymentItem.getUnitAmount());
            paymentItemObject.put("quantity", paymentItem.getQuantity());
            paymentItemObject.put("code", paymentItem.getCode());
            paymentItemsArray.put(paymentItemObject);
        }

        payload.put("paymentItems", paymentItemsArray);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), payload.toString());

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "IremboPaySDK")
                .addHeader("irembopay-secretKey", apiKey)
                .build();
        try {
            Response response = client.newCall(request).execute();
            IremboPayResponse<Invoice> iremboPayResponse = new IremboPayResponse<>();
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            iremboPayResponse.setMessage(responseJsonObject.getString("message"));
            iremboPayResponse.setSuccess(responseJsonObject.getBoolean("success"));
            if (iremboPayResponse.success) {
                iremboPayResponse.setData(getInstanceFromJson(responseJsonObject));
            } else {
                for (Object errorObject : responseJsonObject.getJSONArray("errors")) {
                    JSONObject error = (JSONObject) errorObject;
                    iremboPayResponse.addError(new IremboPayResponse.Error(error.getString("code"), error.getString("detail")));
                }

            }
            return iremboPayResponse;
        } catch (IOException e) {
            // Handle IO exception
            throw new IOException(e);
        }
    }

    private Invoice getInstanceFromJson(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        this.setAmount(data.getFloat("amount"));
        this.setInvoiceNumber(data.getString("invoiceNumber"));
        this.setTransactionId(data.getString("transactionId"));
        this.setCreatedAt(DateToJson.deserialize(data.getString("createdAt")));
        this.setUpdatedAt(DateToJson.deserialize(data.getString("updatedAt")));
        this.setExpiryAt(DateToJson.deserialize(data.getString("expiryAt")));
        this.setDescription(data.getString("description"));
        this.setType(data.getString("type"));
        this.setPaymentStatus(data.getString("paymentStatus"));
        this.setCurrency(data.getString("currency"));
        if (data.has("customer")) {
            Customer responseCustomer = new Customer();

            JSONObject customerObject = data.getJSONObject("customer");
            if (customerObject.has("email"))
                responseCustomer.setEmail(customerObject.getString("email"));
            if (customerObject.has("phoneNumber"))
                responseCustomer.setPhoneNumber(customerObject.getString("phoneNumber"));
            if (customerObject.has("fullName"))
                responseCustomer.setFullName(customerObject.getString("fullName"));
            this.setCustomer(responseCustomer);
        }

        this.setPaymentAccountIdentifier(data.getString("paymentAccountIdentifier"));
        List<PaymentItem> items = new ArrayList<>();
        for (Object paymentItemObject : data.getJSONArray("paymentItems")) {
            PaymentItem paymentItem = new PaymentItem();
            JSONObject paymentObject = (JSONObject) paymentItemObject;
            paymentItem.setQuantity(paymentObject.getInt("quantity"));
            paymentItem.setUnitAmount(paymentObject.getDouble("unitAmount"));
            paymentItem.setCode(paymentObject.getString("code"));
            items.add(paymentItem);
        }
        this.setPaymentItems(items);
        return this;
    }

}
