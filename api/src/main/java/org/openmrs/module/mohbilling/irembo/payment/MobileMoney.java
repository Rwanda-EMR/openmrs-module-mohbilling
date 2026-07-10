package org.openmrs.module.mohbilling.irembo.payment;

import okhttp3.*;

import java.io.IOException;


import org.json.JSONObject;
import org.openmrs.module.mohbilling.irembo.Config;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.IremboPayResponse;

public class MobileMoney extends Config {
    public MobileMoney(String apiKey, Environment environment) {
        super(apiKey, environment);
    }

    public IremboPayResponse<Object> initiate(String accountIdentifier, String paymentProvider, String invoiceNumber, String transactionReference) {
        String url = baseUrl + "/transactions/initiate";
        JSONObject payload = new JSONObject();
        payload.put("accountIdentifier", accountIdentifier);
        payload.put("paymentProvider", paymentProvider);
        payload.put("invoiceNumber", invoiceNumber);
        payload.put("transactionReference", transactionReference);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());


        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "OpenMRS2.6.15")
                .addHeader("irembopay-secretKey", apiKey)
                .build();
        try {
            Response response = client.newCall(request).execute();
            IremboPayResponse<Object> iremboPayResponse = new IremboPayResponse<>();
            assert response.body() != null;
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            iremboPayResponse.setMessage(responseJsonObject.getString("message"));
            iremboPayResponse.setSuccess(responseJsonObject.getBoolean("success"));
            if (!iremboPayResponse.success) {
                for (Object errorObject : responseJsonObject.getJSONArray("errors")) {
                    JSONObject error = (JSONObject) errorObject;
                    iremboPayResponse.addError(new IremboPayResponse.Error(error.getString("code"), error.getString("detail")));
                }
            }
            return iremboPayResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}