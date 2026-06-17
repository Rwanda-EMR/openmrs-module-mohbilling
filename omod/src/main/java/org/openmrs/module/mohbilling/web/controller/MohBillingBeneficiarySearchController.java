package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.mohbilling.web.dwr.MohBillingDWRUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class MohBillingBeneficiarySearchController extends AbstractController {
	
	private final MohBillingDWRUtil mohBillingDWRUtil = new MohBillingDWRUtil();
	
	@Override
	@Authorized({"Manage Admission"})
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String searchString = request.getParameter("searchString");
		String result = "";
		
		if (searchString != null && searchString.trim().length() > 0) {
			result = mohBillingDWRUtil.getBeneficiaryListInTable(searchString.trim());
		}
		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(result);
		return null;
	}
}
