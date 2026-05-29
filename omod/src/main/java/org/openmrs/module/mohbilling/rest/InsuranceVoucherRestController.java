package org.openmrs.module.mohbilling.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherRequest;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class InsuranceVoucherRestController {

	protected final Log log = LogFactory.getLog(getClass());

	private final RhipVoucherService voucherService;

	public InsuranceVoucherRestController(@Autowired RhipVoucherService voucherService) {
		this.voucherService = voucherService;
	}

	@RequestMapping(value = "/rest/v1/mohbilling/insurance/voucher", method = RequestMethod.POST)
	@ResponseBody
	public Object createAndSubmitVoucher(@RequestBody RhipVoucherRequest request) throws ResponseException {
		IntegrationResponse response = voucherService.submitVoucher(request);
		if (response.getErrorMessage() != null) {
			log.warn("Error submitting RHIP voucher: " + response.getErrorMessage());
		}
		return response;
	}
}
