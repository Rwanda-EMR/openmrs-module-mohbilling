package org.openmrs.module.mohbilling.irembo;

import org.openmrs.module.mohbilling.irembo.payment.MobileMoney;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.SignatureHelper;

public class IremboPay extends Config {
    public final Invoice invoice;
    public final MobileMoney mobileMoney;
    public final SignatureHelper signatureHelper;

    public IremboPay(String apiKey, Environment environment) {
        super(apiKey, environment);
        invoice = new Invoice(apiKey, environment);
        mobileMoney = new MobileMoney(apiKey, environment);
        signatureHelper = new SignatureHelper(apiKey);
    }
}
