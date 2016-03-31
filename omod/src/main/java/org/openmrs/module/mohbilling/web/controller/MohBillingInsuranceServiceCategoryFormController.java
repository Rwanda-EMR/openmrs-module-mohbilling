/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 *         This controller backs the /web/module/mohBillingInsuranceRateForm.jsp
 *         page. This controller is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsuranceServiceCategoryFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		try {

			if (request.getParameter("save") != null) {
				boolean saved = handleSaveInsuranceServiceCategory(request, mav);
				if (saved)
					return new ModelAndView(new RedirectView(
							"insuranceServiceCategory.list?insuranceId="
									+ request.getParameter("insuranceId")));
			}

			try {
				mav.addObject("insurance", Context.getService(
						BillingService.class).getInsurance(
						Integer.valueOf(request.getParameter("insuranceId"))));
			} catch (Exception e) {
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,
						"The insurance with id ['"
								+ request.getParameter("insuranceId")
								+ "'] has not been found!");

				e.printStackTrace();
				return new ModelAndView(new RedirectView("insurance.list"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ModelAndView(new RedirectView("insurance.list"));
		}
		return mav;

	}

	private boolean handleSaveInsuranceServiceCategory(
			HttpServletRequest request, ModelAndView mav) {

		Insurance insurance = null;

		try {
			insurance = Context.getService(BillingService.class).getInsurance(
					Integer.valueOf(request.getParameter("insuranceId")));
		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance with id ['"
							+ request.getParameter("insuranceId")
							+ "'] has not been found!");
			log.error("" + e.getMessage());
			e.printStackTrace();

			return false;
		}

		try {

			ServiceCategory sc = new ServiceCategory();

			sc.setName(request.getParameter("serviceCategoryName"));
			sc.setDescription(request
					.getParameter("serviceCategoryDescription"));

			sc.setRetired(false);
			sc.setCreatedDate(new Date());
			sc.setCreator(Context.getAuthenticatedUser());

//			try {
//				sc.setPrice(BigDecimal.valueOf(Long.valueOf(request
//						.getParameter("serviceCategoryPrice"))));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			insurance.addServiceCategory(sc);
			Context.getService(BillingService.class).saveInsurance(insurance);

			request
					.getSession()
					.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"The insurance service category has been saved successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance service category has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		return true;
	}
}
