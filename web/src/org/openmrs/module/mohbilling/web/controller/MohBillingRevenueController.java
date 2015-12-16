package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.advice.MohBillingUsageStatsUtils;
import org.openmrs.module.mohbilling.businesslogic.BillingGlobalProperties;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingRevenueController extends ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String insuranceStr = null, startDateStr = null, endDateStr = null, serviceId = null, cashCollector = null, startHourStr = null, startMinute = null, endHourStr = null, endMinuteStr = null;
		if(request.getParameter("printed")==null){
			
			if (request.getParameter("formStatus") != null
					&& !request.getParameter("formStatus").equals("")) {

				startHourStr = request.getParameter("startHour");
				startMinute = request.getParameter("startMinute");
				endHourStr = request.getParameter("endHour");
				endMinuteStr = request.getParameter("endMinute");

				String startTimeStr = startHourStr + ":" + startMinute + ":00";
				String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
				Date startDate = null, endDate = null;
				if (request.getParameter("startDate") != null
						&& !request.getParameter("startDate").equals("")) {
					startDateStr = request.getParameter("startDate");
					startDate = sdf.parse(startDateStr.split("/")[2] + "-"
							+ startDateStr.split("/")[1] + "-"
							+ startDateStr.split("/")[0] + " " + startTimeStr);
				}

				if (request.getParameter("endDate") != null
						&& !request.getParameter("endDate").equals("")) {
					endDateStr = request.getParameter("endDate");
					endDate = sdf.parse(endDateStr.split("/")[2] + "-"
							+ endDateStr.split("/")[1] + "-"
							+ endDateStr.split("/")[0] + " " + endTimeStr);
				}

				User collector = null;

				if (request.getParameter("cashCollector") != null
						&& !request.getParameter("cashCollector").equals("")) {
					cashCollector = request.getParameter("cashCollector");
					collector = Context.getUserService().getUser(Integer.parseInt(cashCollector));
				}

				if (request.getParameter("insurance") != null)
					insuranceStr = request.getParameter("insurance");

				Integer insuranceIdInt = null;
				Insurance insurance = null;

				User user = Context.getAuthenticatedUser();

				String cashierNames = (user.getPersonName().getFamilyName() != null ? user
						.getPersonName().getFamilyName()
						: "")
						+ " "
						+ (user.getPersonName().getMiddleName() != null ? user
								.getPersonName().getMiddleName() : "")
						+ " "
						+ (user.getPersonName().getGivenName() != null ? user
								.getPersonName().getGivenName() : "");

				/*
				 * if (!request.getParameter("insurance").equals("")) {
				 * insuranceIdInt = Integer.parseInt(insuranceStr); insurance =
				 * InsuranceUtil.getInsurance(insuranceIdInt); }
				 */

				if (startDate != null && endDate != null) {

					BillingService bs = Context.getService(BillingService.class);
					
					
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

					LinkedHashMap<String, Map<String, Double>> basedDateReport = new LinkedHashMap<String, Map<String, Double>>();

					LinkedHashMap<Double, LinkedHashMap<String, Map<String, Double>>> compiledFactures = new LinkedHashMap<Double, LinkedHashMap<String, Map<String, Double>>>();
					// List<PatientBill> bills =
					// bs.getPatientBillsByCollector(receivedDate, collector);

					List<Date> datesBetweenTwoDates = MohBillingUsageStatsUtils
							.getDaysBetweenDates(startDate, endDate);
					// Map for partially paid
					LinkedHashMap<String, Double> partiallPaidMap = new LinkedHashMap<String, Double>();
					// Map for both partially paid and full paid bills
					
				/*	String[] serviceCategories = {"CHIR", "CONSOMM", "CONSULT","ECHOG", "FORMAL", "HOSPITAL","KINE", "LABO", "MATERN","MEDICAM","OXGYENO", "OPHTAL","RADIO",
							"SOINS INF", "STOMAT", "AMBULAN","DOC.LEGAUX","MORGUE"};*/
					List<String> serviceCategories =BillingGlobalProperties.getListofServiceCategory();
					for (Date date : datesBetweenTwoDates) {

						Date startOfDay = sdf.parse(MohBillingUsageStatsUtils.getStartOfDay(date));
						Date endOfDay = sdf.parse(MohBillingUsageStatsUtils.getEndOfDay(date));

						Object[] allfactureCompiled = bs.getBills(startOfDay,	endOfDay, collector);
						Double fullyreceivedAmount = (Double) allfactureCompiled[1];
						Double partialypaids = (Double) allfactureCompiled[2];

						partiallPaidMap.put("Amount Partially Paid", partialypaids);
						Set<PatientBill> patientBills = (Set<PatientBill>) allfactureCompiled[0];

						// All partially paid map where key is
						// "Amount Partiall paid " and value is "Amount" in double

						if (patientBills.size() > 0) {

							Map<String, Double> mappedReport = getAllBillsByCollector(patientBills, serviceCategories,fullyreceivedAmount, partialypaids);
							basedDateReport.put(simpleDateFormat.format(date),	mappedReport);	
							request.getSession().setAttribute("basedDateReport" , basedDateReport);
							
						}					
					}
					
					
					
					
					mav.addObject("collector", collector);
					mav.addObject("serviceCategories", serviceCategories);
					mav.addObject("basedDateReport", basedDateReport);
					mav.addObject("compiledfacture", compiledFactures);
					mav.addObject("printed", "printed");
					
			
		}
		

			}

		}
		
		if (request.getParameter("printed")!=null) {
			String userStr = request.getParameter("userId");			
			
			//User collector =Context.getUserService().getUser(Integer.parseInt(userStr));
					
			LinkedHashMap<String, Map<String, Double>> basedDateReport =  (LinkedHashMap<String, Map<String, Double>>) request.getSession().getAttribute("basedDateReport" );
			FileExporter fexp = new FileExporter();
			String fileName = "daily_report.pdf";
		    fexp.printCashierReport(request, response,basedDateReport,fileName,"Daily Cashier report");			
			
		}
		mav.setViewName(getViewName());

		return mav;
	}

	/**
	 * @param request
	 * @param mav
	 */
	private void rebuildParameters(HttpServletRequest request, ModelAndView mav)
			throws Exception {

		String param = (request.getParameter("startDate") != null) ? "&startDate="
				+ request.getParameter("startDate")
				: "";
		param += (request.getParameter("endDate") != null) ? "&endDate="
				+ request.getParameter("endDate") : "";
		param += (request.getParameter("cashCollector") != null) ? "&cashCollector="
				+ request.getParameter("cashCollector")
				: "";
		param += (request.getParameter("insurance") != null) ? "&insurance="
				+ request.getParameter("insurance") : "";
		mav.addObject("prmtrs", param);
	}

	public static LinkedHashMap<String, Double> getAllBillsByCollector(Set<PatientBill> bills,List<String> serviceCategories,	Double receivedAmount, Double partiallyPaids) {
		//int []docuLegIds ={300,81,5002};
		List<Integer> docuLegIds = Arrays.asList(300,81,5002);
	
		List<Integer> actMorgueIds = Arrays.asList(5011,5046,107,106,105,101,109);
	
		List<Integer> notTonsiderIds = Arrays.asList(5011,5046,107,106,105,101,109,300,81,5002,96);

		LinkedHashMap<String, Double> invoiceMap = new LinkedHashMap<String, Double>();
		Double total = 0.0;
		Double ambulanceCost =0.0;
		//run through all services category and group amount by service category

		for (String svceCateg : serviceCategories) {			

			Double subTotal = 0.0;

			// run through all patient bills  and group amount  by category
			
			for (PatientBill pb : bills) {
				Double currentRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();
				// scan each item amount
				double patDueAmt = 0.0;
				for (PatientServiceBill item : pb.getBillItems()) {
					String category = item.getService().getFacilityServicePrice().getCategory();
					FacilityServicePrice fsp =item.getService().getFacilityServicePrice();
					Integer fspId = fsp.getFacilityServicePriceId();
					
					if (category.startsWith(svceCateg)) {
						
						
						if(notTonsiderIds.contains(fspId)==false){
							
							if (category.equals("AUTRES")) {
								
								double patientCost = item.getQuantity()* item.getUnitPrice().doubleValue();							
								patDueAmt=patDueAmt+patientCost;
								
							} else {
								double patientCost = item.getQuantity()* item.getUnitPrice().doubleValue()* (100 - currentRate) / 100;							
								patDueAmt=patDueAmt+patientCost;

							}
	
						}
												
					}
					//if facilicity service name equal ambulance ,take it away
					if (svceCateg.startsWith("AMBULAN")) {
						if (fspId==96) {
							
		                       double patientCost = item.getQuantity()* item.getUnitPrice().doubleValue()* (100 - currentRate) / 100;							
								
								patDueAmt=patDueAmt+patientCost;
							}						
					}
					//group amount for all  considered  medicaux documents
					if (svceCateg.startsWith("DOC.LEGAUX")) {
												
						if (docuLegIds.contains(fspId)) {
							
		                       double patientCost = item.getQuantity()* item.getUnitPrice().doubleValue()* (100 - currentRate) / 100;							
								
								patDueAmt=patDueAmt+patientCost;
							
						}
						
					}
					//morgue grouping
					
					if (svceCateg.startsWith("MORGUE")) {
						
						if (actMorgueIds.contains(fspId)) {							

		                       double patientCost = item.getQuantity()* item.getUnitPrice().doubleValue()* (100 - currentRate) / 100;							
								
								patDueAmt=patDueAmt+patientCost;
							}
						
					}					
					
					
				}
				subTotal = subTotal + patDueAmt;
			}
		
			// end of all patient bills
			
			invoiceMap.put(svceCateg, ReportsUtil.roundTwoDecimals(subTotal));
			total = total + subTotal;
		}

		// add each invoice linked to catehory service to list of invoice
   
		invoiceMap.put("Total", ReportsUtil.roundTwoDecimals(total));
		invoiceMap.put("receivedAmount", ReportsUtil.roundTwoDecimals(receivedAmount));
		invoiceMap.put("partiallyPaid", ReportsUtil.roundTwoDecimals(partiallyPaids));
		
		return invoiceMap;
	}
}
