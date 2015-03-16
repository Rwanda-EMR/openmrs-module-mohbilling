/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingInsuranceInvoiceController extends
		ParameterizableViewController {

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

			List<Object[]> obj = Context.getService(BillingService.class).getAllServicesByPatient(insurance,
					startDate, endDate, patientId, serviceId, null,
					cashCollector);
			
			

			Double bigTotal = (double) 0;
			Double totalConsultation = (double) 0;
			Double totalHospitalization = (double) 0;
			Double totalKine = (double) 0;
			Double totalPharmacy = (double) 0;
			Double totalEchographie = (double) 0;
			Double totalStomatologie= (double) 0;
			Double totalMaternite= (double) 0;
			Double totalRadio= (double) 0;
			Double totalSoinsInf= (double) 0;
			Double totalFormalite= (double) 0;
			Double totalOxygenotherapie= (double) 0;
			Double totalLabo= (double) 0;
			Double totalOphta= (double) 0;
			
			String category = "";
			
			Map<String, List<Object[]>> map = new HashMap<String, List<Object[]>>();
			List<Object[]> consultations = new ArrayList<Object[]>();
			List<Object[]> hospitalizationServices = new ArrayList<Object[]>();
			List<Object[]> kineServices = new ArrayList<Object[]>();
			List<Object[]> drugsAndCons = new ArrayList<Object[]>();
			List<Object[]> echographieList = new ArrayList<Object[]>();
			List<Object[]> stomatologieList = new ArrayList<Object[]>();
			List<Object[]> materniteList = new ArrayList<Object[]>();
			List<Object[]> radioList = new ArrayList<Object[]>();
			List<Object[]> soinsInfList = new ArrayList<Object[]>();
			List<Object[]> formaliteList = new ArrayList<Object[]>();
			List<Object[]> oxygenotherapieList = new ArrayList<Object[]>();
			List<Object[]> laboList= new ArrayList<Object[]>();
			List<Object[]> ophtaList = new ArrayList<Object[]>();
			
			Map<String,Double> subTotalMap = new HashMap<String, Double>();
			
			for (Object[] ob : obj) {
				BigDecimal cost = (BigDecimal) ob[5];
				bigTotal = cost.doubleValue()+bigTotal.doubleValue();
				
				category = ob[2].toString();
				
				if(category=="CONSULTATION" || category.equals("CONSULTATION")){	
					if(ob!=null){
					consultations.add(ob);			
				    map.put(category, consultations);
				    totalConsultation = totalConsultation.doubleValue()+((BigDecimal)ob[5]).doubleValue();
				    subTotalMap.put(category, totalConsultation);
					}
			   }
				if(category=="HOSPITALISATION" || category.equals("HOSPITALISATION")){	
					if(ob!=null){
					hospitalizationServices.add((Object[])ob);
					totalHospitalization = totalHospitalization.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					map.put(category, hospitalizationServices);
					subTotalMap.put(category, totalHospitalization);
					}
			   }
				if(category=="KINESITHERAPIE" || category.equals("KINESITHERAPIE")){	
					if(ob!=null){
					kineServices.add((Object[])ob);
					map.put(category, kineServices);
					totalKine = totalKine.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalKine);
					}
			   }
				if(category=="MEDICAMENTS" || category.equals("CONSOMMABLES")){	
					if(ob!=null){
					drugsAndCons.add((Object[])ob);
					map.put(category, drugsAndCons);
					totalPharmacy = totalPharmacy.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalPharmacy);
					}
			   }
				if(category=="ECHOGRAPHIE" || category.equals("ECHOGRAPHIE")){	
					if(ob!=null){
					echographieList.add((Object[])ob);
					map.put(category, echographieList);
					totalEchographie = totalEchographie.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalEchographie);
					}
			   }
				if(category=="STOMATOLOGIE" || category.equals("STOMATOLOGIE")){	
					if(ob!=null){
					stomatologieList.add((Object[])ob);
					map.put(category, stomatologieList);
					totalStomatologie = totalStomatologie.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalStomatologie);
					}
			   }
				if(category=="MATERNITE" || category.equals("MATERNITE")){	
					if(ob!=null){
					materniteList.add((Object[])ob);
					map.put(category, materniteList);
					totalMaternite = totalMaternite.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalMaternite);
					}
			   }
				if(category=="RADIOLOGIE" || category.equals("RADIOLOGIE")){	
					if(ob!=null){
					radioList.add((Object[])ob);
					map.put(category, radioList);
					totalRadio = totalRadio.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalRadio);
					}
			   }
				if(category=="SOINS INFIRMIERS" || category.equals("SOINS INFIRMIERS")){	
					if(ob!=null){
					soinsInfList.add((Object[])ob);
					map.put(category, soinsInfList);
					totalSoinsInf = totalSoinsInf.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalSoinsInf);
					}
			   }
				if(category=="FORMALITES ADMINISTRATIVES" || category.equals("FORMALITES ADMINISTRATIVES")){	
					if(ob!=null){
					formaliteList.add((Object[])ob);
					map.put(category, formaliteList);
					totalFormalite = totalFormalite.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalFormalite);
					}
			   }
				if(category=="OXYGENOTHERAPIE" || category.equals("OXYGENOTHERAPIE")){	
					if(ob!=null){
					drugsAndCons.add((Object[])ob);
					map.put(category, drugsAndCons);
					totalPharmacy = totalPharmacy.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalPharmacy);
					}
			   }
				if(category=="LABORATOIRE" || category.equals("LABORATOIRE")){	
					if(ob!=null){
					laboList.add((Object[])ob);
					map.put(category, laboList);
					totalLabo = totalLabo.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalLabo);
					}
			   }
				if(category=="OPHTALMOLOGIE" || category.equals("OPHTALMOLOGIE")){	
					if(ob!=null){
					ophtaList.add((Object[])ob);
					map.put(category, ophtaList);
					totalOphta = totalOphta.doubleValue()+((BigDecimal)ob[5]).doubleValue();
					subTotalMap.put(category, totalOphta);
					}
			   }
			}
			mav.addObject("obj", obj);
			mav.addObject("bigTotal", bigTotal);
			mav.addObject("map", map);
			mav.addObject("subTotalMap", subTotalMap);


		}

		mav.setViewName(getViewName());

		return mav;

	}

}
