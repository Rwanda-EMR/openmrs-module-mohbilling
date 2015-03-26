package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
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

			List<Object[]> obj = Context.getService(BillingService.class).getRevenueOfServices(insurance,
					startDate, endDate, patientId, serviceId, null,
					cashCollector);
			
		//	Map<Date,Map<String,Double>> totalByCategoryMap = new HashMap<Date, Map<String,Double>>();
			

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
			
			Map<String,Double> consultations = new HashMap<String, Double>();
			
			Map<String,Double> laboList = new HashMap<String,Double>();
			
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
			
			Map<String, List<Double>> map = new HashMap<String, List<Double>>();
			Map<String,Double> subTotalMap = new HashMap<String, Double>();
			
			String dateObj[] = null;
			String date = "";
			String temp[] = null;
			
			List<Object[]> totalByCateg = new ArrayList<Object[]>();
			
			Map<String, List<Object[]>> dateAndServicesMap = new HashMap<String, List<Object[]>>();
			
			List<Object[]> services = new ArrayList<Object[]>();
			
			for (Object[] ob : obj) {
//				BigDecimal cost = (BigDecimal) ob[1];
//				bigTotal = cost.doubleValue()+bigTotal.doubleValue();
				
				category = ob[1].toString();
//				dateObj=ob[0].toString().split(" ");
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
				
				if(category=="CONSULTATION" || category.equals("CONSULTATION")){
				    totalConsultation = totalConsultation.doubleValue()+((Double)ob[2]);
//				    consultations.put(date, totalConsultation);
//				    for (String key : consultations.keySet()) {
//				    	if(consultations.keySet().contains(key))
//						totalConsultation=totalConsultation+consultations.get(key);
//					}
			   }
				if(category=="LABORATOIRE" || category.equals("LABORATOIRE")){	
					 totalLabo = totalLabo.doubleValue()+((Double)ob[2]);
//					 laboList.put(date, totalLabo);
//					    for (String key : laboList.keySet()) {
//					    	if(laboList.keySet().contains(key))
//					    		totalLabo=totalLabo+laboList.get(key);
//						}
			   }
//				if(category=="FORMALITES ADMINISTRATIVES" || category.equals("FORMALITES ADMINISTRATIVES")){	
//					if(ob!=null){
//					formaliteList.add((Object[])ob);
//					map.put(date, formaliteList);
//					totalFormalite = totalFormalite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(date, totalFormalite);
//					}
//			   }
//				if(category=="RADIOLOGIE" || category.equals("RADIOLOGIE")){	
//					if(ob!=null){
//					radioList.add((Object[])ob);
//					map.put(date, radioList);
//					totalRadio = totalRadio.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalRadio);
//					}
//			   }
//				if(category=="PEDIATRIC" || category.equals("PEDIATRIC")){	
//					if(ob!=null){
//					pediatList.add((Object[])ob);
//					map.put(date, pediatList);
//					totalPediat = totalPediat.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalPediat);
//					}
//			   }
//				
//				if(category=="ECHOGRAPHIE" || category.equals("ECHOGRAPHIE")){	
//					if(ob!=null){
//					echoList.add((Object[])ob);
//					map.put(date, echoList);
//					totalEcho = totalEcho.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalEcho);
//					}
//			   }
//				if(category=="OPHTALMOLOGIE" || category.equals("OPHTALMOLOGIE")){	
//					if(ob!=null){
//					ophtaList.add((Object[])ob);
//					map.put(category, ophtaList);
//					totalOphta = totalOphta.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalOphta);
//					}
//			   }
//				if(category=="CHIRURGIE" || category.equals("CHIRURGIE")){	
//					if(ob!=null){
//					surgeryList.add((Object[])ob);
//					map.put(category, surgeryList);
//					totalSurgery = totalSurgery.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalSurgery);
//					}
//			   }if(category=="INTERNAL MEDICINE" || category.equals("INTERNAL MEDICINE")){	
//					if(ob!=null){
//					internalMedList.add((Object[])ob);
//					map.put(category, internalMedList);
//					totalInternalMed = totalInternalMed.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalInternalMed);
//					}
//			   }
//				if(category=="GYNECOLOGY" || category.equals("GYNECOLOGY")){	
//					if(ob!=null){
//					gynecoList.add((Object[])ob);
//					map.put(category, gynecoList);
//					totalGyneco = totalGyneco.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalGyneco);
//					}
//			   }
//				if(category=="KINESITHERAPIE" || category.equals("KINESITHERAPIE")){	
//					if(ob!=null){
//					kineList.add((Object[])ob);
//					map.put(category, kineList);
//					totalKine = totalKine.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalKine);
//					}
//			   }
//				if(category=="STOMATOLOGIE" || category.equals("STOMATOLOGIE")){	
//					if(ob!=null){
//					stomatoList.add((Object[])ob);
//					map.put(category, stomatoList);
//					totalStomato = totalStomato.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalStomato);
//					}
//			   }
//				if(category=="MATERNITE" || category.equals("MATERNITE")){	
//					if(ob!=null){
//					MaterniteList.add((Object[])ob);
//					map.put(category, MaterniteList);
//					totalMaternite = totalMaternite.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalMaternite);
//					}
//			   }
//				if(category=="NEONATAL" || category.equals("NEONATAL")){	
//					if(ob!=null){
//					neoList.add((Object[])ob);
//					map.put(category, neoList);
//					totalNeo = totalNeo.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalNeo);
//					}
//			   }
//				if(category=="MEDICAMENTS" || category.equals("MEDICAMENTS")){	
//					if(ob!=null){
//					MedicList.add((Object[])ob);
//					map.put(category, MedicList);
//					totalMedic = totalMedic.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalMedic);
//					}
//			   }
//				if(category=="CONSOMMABLES" || category.equals("CONSOMMABLES")){	
//					if(ob!=null){
//					consummableList.add((Object[])ob);
//					map.put(category, consummableList);
//					totalConsummable = totalConsummable.doubleValue()+((BigDecimal)ob[2]).doubleValue();
//					subTotalMap.put(category, totalConsummable);
//					}
//			   }
//
//				if(category=="OXYGENOTHERAPIE" || category.equals("OXYGENOTHERAPIE")){	
//					if(ob!=null){
//					oxygenotherapieList.add((Object[])ob);
//					map.put(category, oxygenotherapieList);
//					totalOxygenotherapie = totalOxygenotherapie.doubleValue()+((BigDecimal)ob[5]).doubleValue();
//					subTotalMap.put(category,totalOxygenotherapie);
//					}
//				}
				
//				services.add(new Object[]{totalConsultation,totalLabo,totalFormalite,
//						totalRadio,totalPediat,totalEcho,totalOphta,totalSurgery,
//						totalInternalMed,totalGyneco,totalKine,totalStomato,"",totalMaternite,"",
//						totalNeo,"",totalMedic,totalConsummable,"",""});
////				
//////				log.info("cccccccccccccccccccccc"+totalConsultation);
//////				log.info("BBBBBBBBBBBBBBBBBBBBBBB"+totalLabo);
////				
////				log.info("dateeeeeeeeeeeeeeeeeeeeeeeeee "+date);
////				
//				dateAndServicesMap.put(date, services);
				
				totalByCateg.add(new Object[] {date,totalConsultation,totalLabo,totalFormalite,
						totalRadio,totalPediat,totalEcho,totalOphta,totalSurgery,
						totalInternalMed,totalGyneco,totalKine,totalStomato,"",totalMaternite,"",
						totalNeo,"",totalMedic,totalConsummable,"","" });
			}
			
			for (String key : dateAndServicesMap.keySet()) {
				log.info("kkkkkkeyyyyyyyyyyyyyyy "+key);
				for (Object[] ob : dateAndServicesMap.get(key)) {
					log.info("objjjjjjjjjjjjjjjjjjjjj0 DATE :"+ob[0]+"jjjjjjjjjjjjjjjjj "+ob[1]);
					log.info("objjjjjjjjjjjjjjjjjjjjj1 "+ob[1]);
				}
			}
			
			mav.addObject("obj", obj);
			mav.addObject("bigTotal", bigTotal);
			mav.addObject("map", map);
			mav.addObject("subTotalMap", subTotalMap);
			mav.addObject("totalByCateg", totalByCateg);
			mav.addObject("dateAndServicesMap", dateAndServicesMap);
		}

		mav.setViewName(getViewName());

		return mav;

	}
	
	public Double getAmount(List<Object[]> dates){
		return null;
		
	}
	
}
