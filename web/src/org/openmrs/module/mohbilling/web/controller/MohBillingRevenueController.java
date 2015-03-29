package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingRevenueController extends ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<String> categories = InsuranceUtil.getAllServiceCategories();
		List<BillPayment> reportedPayments = new ArrayList<BillPayment>();

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());
		mav.addObject("categories", categories);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String patientIdStr = null, insuranceStr = null, 
				startDateStr = null, endDateStr = null, serviceId = null, 
				cashCollector = null, startHourStr = null, startMinute = null, 
				endHourStr = null, endMinuteStr = null;

		if (request.getParameter("formStatus") != null && !request.getParameter("formStatus").equals("")) {
			
			startHourStr = request.getParameter("startHour");
			startMinute = request.getParameter("startMinute"); 
			endHourStr = request.getParameter("endHour");
			endMinuteStr = request.getParameter("endMinute");

			String startTimeStr = startHourStr + ":" + startMinute + ":00";
			String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
			Date startDate = null, endDate = null;
			if(request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")) {
				startDateStr = request.getParameter("startDate");
				startDate = sdf.parse(startDateStr.split("/")[2] + "-"
						+ startDateStr.split("/")[1] + "-"
						+ startDateStr.split("/")[0] + " " + startTimeStr);
			}
			
			if(request.getParameter("endDate") != null && !request.getParameter("endDate").equals("")) {
				endDateStr = request.getParameter("endDate");
				endDate = sdf.parse(endDateStr.split("/")[2] + "-"
						+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
						+ " " + endTimeStr);
			}
			
			if(request.getParameter("patientId") != null)
				patientIdStr = request.getParameter("patientId");
				
			if(request.getParameter("cashCollector") != null)
				cashCollector = request.getParameter("cashCollector");
			
			
			if(request.getParameter("insurance") != null)
				insuranceStr = request.getParameter("insurance");			

			Integer insuranceIdInt = null;
			Integer patientId = null;
			// Date endDate = null;
			Insurance insurance = null;
			String patientNames = null;

			User user = Context.getAuthenticatedUser();

			String cashierNames = (user.getPersonName().getFamilyName() != null ? user
					.getPersonName().getFamilyName() : "")
					+ " "
					+ (user.getPersonName().getMiddleName() != null ? user
							.getPersonName().getMiddleName() : "")
					+ " "
					+ (user.getPersonName().getGivenName() != null ? user
							.getPersonName().getGivenName() : "");

			if (!request.getParameter("patientId").equals("")) {

				patientIdStr = request.getParameter("patientId");
				patientId = Integer.parseInt(patientIdStr);
				Patient patient = Context.getPatientService().getPatient(
						patientId);
				patientNames = (patient.getFamilyName() != null ? patient
						.getFamilyName() : "")
						+ " "
						+ (patient.getMiddleName() != null ? patient
								.getMiddleName() : "")
						+ " "
						+ (patient.getGivenName() != null ? patient
								.getGivenName() : "");
			}

			if (!request.getParameter("insurance").equals("")) {
				insuranceIdInt = Integer.parseInt(insuranceStr);
				insurance = InsuranceUtil.getInsurance(insuranceIdInt);
			}

			List<Date> dates = Context.getService(BillingService.class).getRevenueDates(insurance,
					startDate, endDate, patientId, serviceId, null,
					cashCollector);

			Double bigTotal = (double) 0;
			
			Map<Date, List<Object[]>> map = new HashMap<Date, List<Object[]>>();
			Map<String,Double> subTotalMap = new HashMap<String, Double>();
			
			BillingService service=Context.getService(BillingService.class);
			
			for (Date date : dates) {
				
				List<Object[]> totalByCateg = new ArrayList<Object[]>();
				
				Double totalConsultation = 0.0;
				Double totalLabo= 0.0;
				Double totalFormalite= (double) 0;
				Double totalRadio= (double) 0;
				Double totalPediat= (double) 0;
				Double totalEcho= (double) 0;
				Double totalOphta= (double) 0;
				Double totalSurgery= (double) 0;
				Double totalInternalMed= (double) 0;
				Double totalGyneco= (double) 0;
				Double totalKine = (double) 0;
				Double totalStomato= (double) 0;
				Double totalPetiteChir= (double) 0;
				Double totalMaternite= (double) 0;
				Double totalClinic= (double) 0;
				Double totalNeo= (double) 0;
				Double totalAmbulance= (double) 0;
				Double totalMedic= (double) 0;
				Double totalConsummable= (double) 0;
				Double totalMorgue= (double) 0;
				Double totalAutre= (double) 0;			
				Double totalOxygenotherapie= (double) 0;
				Double totalSoinsInf = (double) 0;
				

				totalConsultation = (Double)service.getSum("CONSULTATION", date);
				totalLabo=(Double)service.getSum("LABORATOIRE", date);
				totalFormalite=(Double)service.getSum("FORMALITES ADMINISTRATIVES", date);
				totalRadio=(Double)service.getSum("RADIOLOGIE", date);
				totalPediat=(Double)service.getSum("PEDIATRIC", date);
				totalEcho=(Double)service.getSum("ECHOGRAPHIE", date);
				totalOphta=(Double)service.getSum("OPHTALMOLOGIE", date);
				totalSurgery=(Double)service.getSum("CHIRURGIE", date);
				totalInternalMed=(Double)service.getSum("INTERNAL MEDICINE", date);
				totalGyneco=(Double)service.getSum("GYNECOLOGY", date);
				totalKine=(Double)service.getSum("KINESITHERAPIE", date);
				totalStomato=(Double)service.getSum("STOMATOLOGIE", date);
				totalMaternite=(Double)service.getSum("MATERNITE", date);
				totalNeo=(Double)service.getSum("NEONATAL", date);
				totalMedic=(Double)service.getSum("MEDICAMENTS", date);
				totalConsummable=(Double)service.getSum("CONSOMMABLES", date);
				totalOxygenotherapie=(Double)service.getSum("OXYGENOTHERAPIE", date);

				
				//list of services' revenues
				totalByCateg.add(new Object[] {totalConsultation,totalLabo,totalFormalite,
						totalRadio,totalPediat,totalEcho,totalOphta,totalSurgery,
						totalInternalMed,totalGyneco,totalKine,totalStomato,"",totalMaternite,"",
						totalNeo,"",totalMedic,totalConsummable,"","" });
				
				//this map help to display a date once
				map.put(date, totalByCateg);
			}
			
			// total of the columns
			Double colCons = 0.0;
			for (Date date : map.keySet()) {
				for (Object[] col : map.get(date)) {
					if(col[0]!=null)
					colCons=colCons+(Double)col[0];
				}
			}
			mav.addObject("map", map);
			mav.addObject("subTotalMap", subTotalMap);
			mav.addObject("bigTotal", bigTotal);
			
			mav.addObject("colCons", colCons);
		}

		mav.setViewName(getViewName());

		return mav;

	}
	
	
}
