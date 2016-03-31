/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 *         This controller backs the
 *         /web/module/mohBillingInsuranceServiceCategory.jsp page. This
 *         controller is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsuranceServiceCategoryListController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		try {
			mav.addObject("insurance", Context.getService(BillingService.class)
					.getInsurance(
							Integer
									.valueOf(request
											.getParameter("insuranceId"))));
		} catch (Exception e) {
			e.printStackTrace();
			return new ModelAndView(new RedirectView("insurance.list"));
		}
		return mav;

	}
}
