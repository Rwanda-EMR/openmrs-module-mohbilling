package org.openmrs.module.mohbilling.irembo;

import org.openmrs.module.mohbilling.irembo.util.Environment;

abstract public class Config {
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
}
