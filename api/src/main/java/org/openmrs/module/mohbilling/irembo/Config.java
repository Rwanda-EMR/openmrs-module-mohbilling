package org.openmrs.module.mohbilling.irembo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import org.openmrs.module.mohbilling.irembo.util.Environment;

abstract public class Config {
    /**
     * Irembo Pay v2 responses include paymentLinkUrl on invoice and batch invoice payloads.
     */
    protected static final String API_VERSION = "2";

    /**
     * Time allowed to establish a TCP connection (and complete TLS handshake) to Irembo Pay.
     * OkHttp default is 10s; hospital networks often need longer.
     */
    protected static final long CONNECT_TIMEOUT_SECONDS = 60L;
    /**
     * Time allowed between consecutive bytes while reading the response body.
     */
    protected static final long READ_TIMEOUT_SECONDS = 90L;
    /**
     * Time allowed between consecutive bytes while writing the request body.
     */
    protected static final long WRITE_TIMEOUT_SECONDS = 60L;
    /**
     * Hard ceiling for the entire call (connect + write + read). Slightly above read so
     * a slow response can still complete after a long connect.
     */
    protected static final long CALL_TIMEOUT_SECONDS = 120L;

    private static final OkHttpClient SHARED_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    protected String apiKey;
    protected Environment environment;
    protected String baseUrl;

    public Config(String apiKey, Environment environment) {
        this.apiKey = apiKey;
        this.environment = environment;
        switch (environment) {
            case SANDBOX:
                baseUrl = "https://api.sandbox.irembopay.com/payments";
                break;
            case CHECKOUT:
                baseUrl = "https://checkout.uat.irembopay.com/payments";
                break;
            case PRODUCTION:
                baseUrl = "https://api.irembopay.com/payments";
                break;

        }
    }

    /**
     * Shared OkHttp client for all Irembo Pay HTTP calls (connection pooling + consistent timeouts).
     */
    protected OkHttpClient httpClient() {
        return SHARED_HTTP_CLIENT;
    }

    protected void applyStandardHeaders(okhttp3.Request.Builder builder) {
        builder.addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("irembopay-secretKey", apiKey)
                .addHeader("X-API-Version", API_VERSION)
                .addHeader("User-Agent", "IremboPaySDK");
    }

    protected okhttp3.Request.Builder newAuthenticatedRequest(String url) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        applyStandardHeaders(builder);
        return builder;
    }

    protected String checkoutBaseUrl() {
        if (environment == Environment.PRODUCTION) {
            return "https://checkout.irembopay.com/";
        }
        if (environment == Environment.CHECKOUT) {
            return "https://checkout.uat.irembopay.com/";
        }
        return "https://checkout.sandbox.irembopay.com/";
    }
}
