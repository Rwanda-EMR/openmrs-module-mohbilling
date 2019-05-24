/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author rbcemr
 *
 */
public class MohBillingApprovePatientBillController extends ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Consommation consommation = ConsommationUtil.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
		PatientBill pb =consommation.getPatientBill();

		String approved="NO";
		User approvedBy= Context.getAuthenticatedUser();
		Date date = new Date();
		Date approvedDate=null;
		try {
			approvedDate = df.parse(df.format(date));
		}catch (ParseException e){
			e.printStackTrace();
		}

		if(request.getParameter("approvebill")!=null){
			approved="YES";
		}

		String disapproveReason=null;
		if(request.getParameter("disapprovereason")!=null || request.getParameter("disapprovereason")!="") {
			disapproveReason=request.getParameter("disapprovereason");
		}
		pb.setApproved(approved);
		pb.setDisapproveReason(disapproveReason);

		pb.setApprovedDate(approvedDate);
		pb.setApprovedBy(approvedBy);

		Context.getService(BillingService.class).savePatientBill(pb);

		mav.addObject("consommationId", consommation.getConsommationId());

		return new ModelAndView( "redirect:/module/mohbilling/patientBillPayment.form?consommationId="+consommation.getConsommationId()+"");
		//return mav;
	}

}
