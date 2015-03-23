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

			List<Object[]> obj = Context.getService(BillingService.class).getRevenueOfServices(insurance,
					startDate, endDate, patientId, serviceId, null,
					cashCollector);
			
			Map<Date,Map<String,Double>> totalByCategoryMap = new HashMap<Date, Map<String,Double>>();
			

			Double bigTotal = (double) 0;
			
			Double totalConsultation = (double) 0;
			Double totalLabo= (double) 0;
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

			String category = "";
			
			List<Object[]> consultationList = new ArrayList<Object[]>();
			List<Object[]> laboList = new ArrayList<Object[]>();
			List<Object[]> formaliteList = new ArrayList<Object[]>();
			List<Object[]> radioList = new ArrayList<Object[]>();
			List<Object[]> pediatList = new ArrayList<Object[]>();
			List<Object[]> echoList = new ArrayList<Object[]>();
			List<Object[]> ophtaList = new ArrayList<Object[]>();
			List<Object[]> surgeryList = new ArrayList<Object[]>();
			List<Object[]> internalMedList = new ArrayList<Object[]>();
			List<Object[]> gynecoList = new ArrayList<Object[]>();
			List<Object[]> kineList = new ArrayList<Object[]>();
			List<Object[]> stomatoList = new ArrayList<Object[]>();
			List<Object[]> petiteChirList = new ArrayList<Object[]>();
			List<Object[]> MaterniteList = new ArrayList<Object[]>();
			List<Object[]> clinicList = new ArrayList<Object[]>();
			List<Object[]> neoList = new ArrayList<Object[]>();
			List<Object[]> ambulanceList = new ArrayList<Object[]>();
			List<Object[]>  MedicList = new ArrayList<Object[]>();
			List<Object[]>  consummableList = new ArrayList<Object[]>();
			List<Object[]>  morgueList = new ArrayList<Object[]>();
			List<Object[]>  autreList = new ArrayList<Object[]>();	
			List<Object[]>  oxygenotherapieList = new ArrayList<Object[]>();
			
			List<Object[]>  soinsInfList = new ArrayList<Object[]>();
			
			Map<String, List<Object[]>> map = new HashMap<String, List<Object[]>>();
			Map<String,Double> subTotalMap = new HashMap<String, Double>();
			
			String dateObj[] = null;
			String date = "";
			String temp[] = null;
			
			List<Object[]> totalByCateg = new ArrayList<Object[]>();
			
			for (Object[] ob : obj) {
//				BigDecimal cost = (BigDecimal) ob[1];
//				bigTotal = cost.doubleValue()+bigTotal.doubleValue();
				
				category = ob[1].toString();
				dateObj=ob[0].toString().split(" ");
				date=ob[0].toString();
				
				totalConsultation =  0.0;
				totalLabo= 0.0;
				totalFormalite= 0.0;
				totalPediat= 0.0;
				totalEcho= 0.0;
				totalOphta= 0.0;
				totalSurgery= 0.0;
				totalInternalMed= 0.0;
				totalGyneco= 0.0;
				totalKine = 0.0;
				totalStomato= 0.0;
				totalPetiteChir= 0.0;
				totalMaternite= 0.0;
				totalClinic= 0.0;
				totalNeo= 0.0;
				totalAmbulance= 0.0;
				totalMedic= 0.0;
				totalConsummable= 0.0;
				totalMorgue= 0.0;
				totalAutre= 0.0;			
				totalOxygenotherapie= 0.0;
				totalSoinsInf = 0.0;

				
				log.info("zzzzzzzzzzzzzzzzzzzzzzzzzz "+date);
				
				if(category=="CONSULTATION" || category.equals("CONSULTATION")){	
					if(ob!=null){
					consultationList.add(ob);
				    map.put(date, consultationList);
				    temp = date.split(" ");
				    totalConsultation = totalConsultation.doubleValue()+((BigDecimal)ob[2]).doubleValue();
				    subTotalMap.put(category, totalConsultation);
					}
			   }
				if(category=="LABORATOIRE" || category.equals("LABORATOIRE")){	
					if(ob!=null){
						laboList.add(ob);			
					    map.put(date, laboList);
					    totalLabo = totalLabo.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					    subTotalMap.put(category, totalLabo);
						}
			   }
				if(category=="FORMALITES ADMINISTRATIVES" || category.equals("FORMALITES ADMINISTRATIVES")){	
					if(ob!=null){
					formaliteList.add((Object[])ob);
					map.put(date, formaliteList);
					totalFormalite = totalFormalite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalFormalite);
					}
			   }
				if(category=="RADIOLOGIE" || category.equals("RADIOLOGIE")){	
					if(ob!=null){
					formaliteList.add((Object[])ob);
					map.put(date, formaliteList);
					totalFormalite = totalFormalite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalFormalite);
					}
			   }
				if(category=="ECHOGRAPHIE" || category.equals("ECHOGRAPHIE")){	
					if(ob!=null){
					formaliteList.add((Object[])ob);
					map.put(date, formaliteList);
					totalFormalite = totalFormalite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalFormalite);
					}
			   }
				
				if(category=="MEDICAMENTS" || category.equals("CONSOMMABLES")){	
					if(ob!=null){
					MedicList.add((Object[])ob);
					map.put(category, MedicList);
					totalMedic = totalMedic.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalMedic);
					}
			   }
				if(category=="OPHTALMOLOGIE" || category.equals("OPHTALMOLOGIE")){	
					if(ob!=null){
					echoList.add((Object[])ob);
					map.put(category, echoList);
					totalEcho = totalEcho.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalEcho);
					}
			   }
				if(category=="CHIRURGIE" || category.equals("CHIRURGIE")){	
					if(ob!=null){
					stomatoList.add((Object[])ob);
					map.put(category, stomatoList);
					totalStomato = totalStomato.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalStomato);
					}
			   }
				if(category=="KINESITHERAPIE" || category.equals("KINESITHERAPIE")){	
					if(ob!=null){
					MaterniteList.add((Object[])ob);
					map.put(category, MaterniteList);
					totalMaternite = totalMaternite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalMaternite);
					}
			   }
				if(category=="STOMATOLOGIE" || category.equals("STOMATOLOGIE")){	
					if(ob!=null){
					radioList.add((Object[])ob);
					map.put(category, radioList);
					totalRadio = totalRadio.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalRadio);
					}
			   }
				if(category=="MATERNITE" || category.equals("MATERNITE")){	
					if(ob!=null){
					soinsInfList.add((Object[])ob);
					map.put(category, soinsInfList);
					totalSoinsInf = totalSoinsInf.doubleValue()+((BigDecimal)ob[2]).doubleValue();
					subTotalMap.put(category, totalSoinsInf);
					}
			   }

//				if(category=="OXYGENOTHERAPIE" || category.equals("OXYGENOTHERAPIE")){	
//					if(ob!=null){
//					drugsAndCons.add((Object[])ob);
//					map.put(category, drugsAndCons);
//					totalPharmacy = totalPharmacy.doubleValue()+((BigDecimal)ob[5]).doubleValue();
//					subTotalMap.put(category, totalPharmacy);
//					}
//			   }
//				if(category=="LABORATOIRE" || category.equals("LABORATOIRE")){	
//					if(ob!=null){
//					laboList.add((Object[])ob);
//					map.put(category, laboList);
//					totalLabo = totalLabo.doubleValue()+((BigDecimal)ob[5]).doubleValue();
//					subTotalMap.put(category, totalLabo);
//					}
//			   }
//				if(category=="OPHTALMOLOGIE" || category.equals("OPHTALMOLOGIE")){	
//					if(ob!=null){
//					ophtaList.add((Object[])ob);
//					map.put(category, ophtaList);
//					totalOphta = totalOphta.doubleValue()+((BigDecimal)ob[5]).doubleValue();
//					subTotalMap.put(category, totalOphta);
//					}
//			   }
				totalByCateg.add(new Object[] {date,totalConsultation,totalLabo,totalFormalite,totalRadio,totalPediat,totalEcho,totalOphta,totalSurgery,totalGyneco,totalKine,totalStomato,totalMaternite });
			}
			
			
			mav.addObject("obj", obj);
			mav.addObject("bigTotal", bigTotal);
			mav.addObject("map", map);
			mav.addObject("subTotalMap", subTotalMap);
			mav.addObject("totalConsultation", totalConsultation);
			mav.addObject("totalLabo", totalLabo);
			mav.addObject("totalFormalite", totalFormalite);
			mav.addObject("totalByCateg", totalByCateg);
		}

		mav.setViewName(getViewName());

		return mav;

	}

}
