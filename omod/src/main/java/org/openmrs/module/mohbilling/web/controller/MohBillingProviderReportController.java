package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
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
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {		ModelAndView mav = new ModelAndView();
        mav.setViewName(getViewName());


        if (request.getParameter("formStatus") != null
                && !request.getParameter("formStatus").equals("")) {
            String startDateStr = request.getParameter("startDate");
            String startHourStr = request.getParameter("startHour");
            String startMinStr = request.getParameter("startMinute");

            String endDateStr = request.getParameter("endDate");
            String endHourStr = request.getParameter("endHour");
            String endMinuteStr = request.getParameter("endMinute");

            String collectorStr = null;
            String insuranceStr = null;
            String thirdPartyStr = null;
            String billStatus = null;
            String departmentStr = null;


            User creator = null;
            Department department = null;

            String billCreatorStr = null;
            if(request.getParameter("billCreator")!=null &&!request.getParameter("billCreator").equals("")){
                billCreatorStr=request.getParameter("billCreator");
                creator = Context.getUserService().getUser(Integer.valueOf(billCreatorStr));
            }
            if(request.getParameter("billStatus")!=null&&!request.getParameter("billStatus").equals(""))
                billStatus = request.getParameter("billStatus");

            if(request.getParameter("departmentId")!=null&&!request.getParameter("departmentId").equals("")){
                departmentStr = request.getParameter("departmentId");
                department = DepartementUtil.getDepartement(Integer.valueOf(departmentStr));
            }

            // parameters
            Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);

            Date startDate = (Date) params[0];
            Date endDate = (Date) params[1];

            Insurance insurance = null;
            ThirdParty thirdParty =null;
            if(request.getParameter("insuranceId")!=null &&!request.getParameter("insuranceId").equals("") ){
                insurance = InsuranceUtil.getInsurance(Integer.valueOf(request.getParameter("insuranceId")));
            }
            if(request.getParameter("thirdPartyId")!=null && !request.getParameter("thirdPartyId").equals(""))
                thirdParty = Context.getService(BillingService.class).getThirdParty(Integer.valueOf(request.getParameter("thirdPartyId")));

            List<Consommation> cons = ConsommationUtil.getDCPConsommations(startDate, endDate,creator);
            List<Consommation> consFilteredByBillStatus=new ArrayList<Consommation>();
            if(billStatus!=null){
                for (Consommation con:cons){
                    BigDecimal pamount=new BigDecimal(0);
                    if(con.getPatientBill().getPayments().size()>0)
                        for(BillPayment bp:con.getPatientBill().getPayments()){
                            pamount=pamount.add(bp.getAmountPaid());
                        }
                    if(con.getPatientBill().getPayments().size()>=0 && (con.getPatientBill().getAmount().compareTo(pamount)==-1 || con.getPatientBill().getAmount().compareTo(pamount)==0) && billStatus.toString().equals("FULLY PAID")){
                        consFilteredByBillStatus.add(con);
                    }
                    if(con.getPatientBill().getPayments().size()>=0 && (con.getPatientBill().getAmount().compareTo(pamount)==1) && billStatus.toString().equals("PARTLY PAID")){
                        consFilteredByBillStatus.add(con);
                    }
                    if(con.getPatientBill().getPayments().size()==0 && billStatus.toString().equals("UNPAID")){
                        consFilteredByBillStatus.add(con);
                    }
                }
                mav.addObject("consommations", consFilteredByBillStatus);
            }
            else {
                request.getSession().setAttribute("consommations",cons);
                mav.addObject("consommations", cons);
            }
            cons.get(0).getPatientBill().getStatus();

            mav.addObject("resultMsg", " Bill from "+startDateStr +" To "+ endDateStr);

        }
        mav.addObject("insurances", InsuranceUtil.getAllInsurances());
        mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());
        mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());
        if(request.getParameter("export")!=null){
            List<Consommation> consommations = (List<Consommation>) request.getSession().getAttribute("consommations" );
            FileExporter.exportDCPData(request, response, consommations);
        }
        return mav;
    }
}
