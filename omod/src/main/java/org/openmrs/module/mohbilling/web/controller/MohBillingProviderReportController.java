package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingProviderReportController extends ParameterizableViewController {
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(getViewName());
        if (request.getParameter("formStatus") != null
                && !request.getParameter("formStatus").equals("")) {
            String startDateStr = request.getParameter("startDate");
            String startHourStr = request.getParameter("startHour");
            String startMinStr = request.getParameter("startMinute");
            String endDateStr = request.getParameter("endDate");
            String endHourStr = request.getParameter("endHour");
            String endMinuteStr = request.getParameter("endMinute");
            User creator = null;
            String billCreatorStr = null;
            if(request.getParameter("billCreator")!=null &&!request.getParameter("billCreator").equals("")){
                billCreatorStr=request.getParameter("billCreator");
                creator = Context.getUserService().getUser(Integer.valueOf(billCreatorStr));
            }
            // parameters
            Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, null, null, null);

            Date startDate = (Date) params[0];
            Date endDate = (Date) params[1];
            BigDecimal total100 = new BigDecimal(0);
            List<BigDecimal> totals = new ArrayList<BigDecimal>();

            List<String> columns = new ArrayList<String>();
            List<AllServiceRevenueCons> listOfAllServicesRevenue =new ArrayList<AllServiceRevenueCons>();
            Float insuRate =null;
            Consommation initialConsom=null;

            List<Consommation> cons = ConsommationUtil.getDCPConsommations(startDate, endDate,creator);
            int countCons=1;
            for (Consommation con : cons) {
                initialConsom = con;
                insuRate = con.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
                List<PatientServiceBill> billItems = new ArrayList<PatientServiceBill>();
                for (PatientServiceBill psb : con.getBillItems()) {
                    if(psb.getItemType()==2 && !psb.isVoided()){
                        billItems.add(psb);
                    }
                }
                List<ServiceRevenue> insuranceColumnsRevenues=new ArrayList<ServiceRevenue>();
                BigDecimal totalConsAmount=new BigDecimal(0);
                BigDecimal totalASR = new BigDecimal(0);
                BigDecimal totalPatientASR = new BigDecimal(0);
                List<HopService>  reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.insuranceReportColumnsDcp");
                totalConsAmount=totalConsAmount.add(ReportsUtil.getTotalByItems(billItems));
                totalASR = totalConsAmount.multiply(BigDecimal.valueOf(insuRate)).divide(new BigDecimal(100));
                totalPatientASR = totalConsAmount.multiply(BigDecimal.valueOf(100-insuRate)).divide(new BigDecimal(100));


                for (HopService hopService : reportColumns) {
                    if (!columns.contains(hopService.getName())) {
                        columns.add(hopService.getName());
                    }
                    insuranceColumnsRevenues.add(ReportsUtil.getServiceRevenues(billItems, hopService));
                }
                AllServiceRevenueCons servicesRevenu = new AllServiceRevenueCons(new BigDecimal(0), new BigDecimal(0), "2016-09-11");
                servicesRevenu.setRevenues(insuranceColumnsRevenues);
                servicesRevenu.setAllDueAmounts(totalConsAmount);
                servicesRevenu.setBigTotal(totalASR);
                servicesRevenu.setPatientTotal(totalPatientASR);
                servicesRevenu.setConsommation(initialConsom);
                listOfAllServicesRevenue.add(servicesRevenu);
                countCons ++;
            }
            List<PatientServiceBill> allItems = ReportsUtil.getBillItemsByAllConsommations(cons);
            for (String category : columns) {
                totals.add(ReportsUtil.getTotalByCategorizedItems(allItems, category));
                total100=total100.add(ReportsUtil.getTotalByCategorizedItems(allItems, category));
            }

            request.getSession().setAttribute("columns" , columns);
            request.getSession().setAttribute("listOfAllServicesRevenue" , listOfAllServicesRevenue);

            mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
            mav.addObject("resultMsg", " Bill from "+startDateStr +" To "+ endDateStr);
            mav.addObject("columns", columns);
            mav.addObject("insuranceRate" , insuRate);
            mav.addObject("totals", totals);
            mav.addObject("total100", total100);
        }
        if(request.getParameter("export")!=null){
            List<String> columns = (List<String>) request.getSession().getAttribute("columns");
            List<AllServiceRevenueCons> listOfAllServicesRevenue = (List<AllServiceRevenueCons>) request.getSession().getAttribute("listOfAllServicesRevenue" );
            FileExporter.exportDCPData(request, response, listOfAllServicesRevenue,columns);
        }
        return mav;
    }
}