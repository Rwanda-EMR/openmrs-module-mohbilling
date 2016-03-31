/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.lib.HashSet;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 *         This controller backs the
 *         /web/module/mohBillingInsurancePolicySearchForm.jsp page. This
 *         controller is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsurancePolicySearchFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		// set the minimum number of character to be typed before starting
		// searching
		// this will be retrieved from GPs in order to be the same as the one
		// used by OpenMRS
		Integer numberOfCharacters = Integer.valueOf(Context
				.getAdministrationService().getGlobalPropertyObject(
						"minSearchCharacters").getPropertyValue());
		try {

			GlobalProperty gpMinSearchCharacters = Context
					.getAdministrationService().getGlobalPropertyObject(
							"minSearchCharacters");

			if (gpMinSearchCharacters != null
					&& gpMinSearchCharacters.getProperty().trim().compareTo("") != 0) {
				numberOfCharacters = Integer.valueOf(gpMinSearchCharacters
						.getPropertyValue().trim());
			}
		} catch (Exception e) {

			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance policy could not be saved !");
			
			log.error(">>>>MOH>>>BILLING>> An error occured when trying "
					+ "to load the GP minSearchCharacters" + e.getMessage());
			
			e.printStackTrace();
		}	


		mav.addObject("minSearchCharacters", numberOfCharacters);

		return mav;

	}
}
