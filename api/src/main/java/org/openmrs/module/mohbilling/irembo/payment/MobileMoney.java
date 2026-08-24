package org.openmrs.module.mohbilling.irembo.payment;

import okhttp3.*;

import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.module.mohbilling.irembo.Config;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.IremboPayLogUtil;
import org.openmrs.module.mohbilling.irembo.util.IremboPayNetworkUtil;
import org.openmrs.module.mohbilling.irembo.util.IremboPayResponse;

public class MobileMoney extends Config {
    private static final Log log = LogFactory.getLog(MobileMoney.class);

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

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());

        Request request = newAuthenticatedRequest(url).post(body).build();
        try {
            Response response = httpClient().newCall(request).execute();
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
            String host = IremboPayNetworkUtil.hostFromBaseUrl(baseUrl);
            String details = IremboPayNetworkUtil.describeFailure(e, host);
            IremboPayLogUtil.logFailure(log, "HTTP_INITIATE_PAYMENT",
                    "initiate payment I/O error, invoiceNumber=" + invoiceNumber
                            + ", provider=" + paymentProvider + ", transactionReference=" + transactionReference
                            + ", " + details,
                    e);
            throw new RuntimeException(details, e);
        }


    }
}