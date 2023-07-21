package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class MohBillingInsuranceReportController extends
        ParameterizableViewController {
    protected final Log log = LogFactory.getLog(getClass());

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView mav = new ModelAndView();
        mav.setViewName(getViewName());
        mav.addObject("insurances", InsuranceUtil.getAllInsurances());

        Insurance insurance = null;

        List<AllServicesReportRevenue> listOfAllServicesRevenue = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        List<BigDecimal> totals = new ArrayList<BigDecimal>();
        BigDecimal total100 = BigDecimal.ZERO;
        BigDecimal insuranceFlatFee = BigDecimal.ZERO;

        if (request.getParameter("formStatus") != null
                && !request.getParameter("formStatus").equals("")) {

            Integer insuranceId = Integer.valueOf(request.getParameter("insuranceId"));
            insurance = InsuranceUtil.getInsurance(insuranceId);
            InsuranceRate insuranceRate = insurance.getCurrentRate();

            String startDateStr = request.getParameter("startDate");
            String startHourStr = request.getParameter("startHour");
            String startMinStr = request.getParameter("startMinute");

            String endDateStr = request.getParameter("endDate");
            String endHourStr = request.getParameter("endHour");
            String endMinuteStr = request.getParameter("endMinute");

            // parameters
            Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr,
                    endDateStr, endHourStr, endMinuteStr, null, null, null);

            Date startDate = (Date) params[0];
            Date endDate = (Date) params[1];

            try {

                if (startDate != null && endDate != null) {

                    List<PatientServiceBillReport> bills = ReportsUtil.getPatientServiceBillReport(insuranceId, startDate, endDate);
                    Map<Integer, List<PatientServiceBillReport>> billsMap = new HashMap<>();

                    for (PatientServiceBillReport bill : bills) {
                        billsMap.computeIfAbsent(bill.getGlobalBillId(), k -> new LinkedList<>()).add(bill);
                    }
                    System.out.println("GlobalBills map size: " + bills.size());

                    List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.insuranceReportColumns");
                    List<HopService> imagingServices = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.IMAGING");
                    List<HopService> procedureServices = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.PROCEDURES");

                    reportColumns.addAll(imagingServices);
                    reportColumns.addAll(procedureServices);

                    columns.addAll(getColumns(reportColumns));
                    System.out.println("Columns here 1: " + columns);

                    for (Map.Entry<Integer, List<PatientServiceBillReport>> entry : billsMap.entrySet()) {

                        List<ServiceReportRevenue> billRevenues = new ArrayList<>();
                        for (HopService hopService : reportColumns) {
                            billRevenues.add(ReportsUtil.getServiceReportRevenues(entry.getValue(), hopService));
                        }

                        BigDecimal allDueAmounts = BigDecimal.ZERO;
                        allDueAmounts = allDueAmounts.add(ReportsUtil.getTotalByBillReportItems(entry.getValue()));

                        PatientServiceBillReport initialReport = entry.getValue().get(0);
                        initialReport.setCurrentInsuranceRate(insuranceRate.getRate());
                        initialReport.setCurrentInsuranceRateFlatFee(insuranceRate.getFlatFee().doubleValue());

                        AllServicesReportRevenue servicesRevenue = new AllServicesReportRevenue(BigDecimal.ZERO, BigDecimal.ZERO, "2016-09-11");
                        servicesRevenue.setRevenues(billRevenues);
                        servicesRevenue.setAllDueAmounts(allDueAmounts);
                        servicesRevenue.setConsommation(initialReport);//get any, they have some similar values we want

                        listOfAllServicesRevenue.add(servicesRevenue);
                    }

                    for (String serviceName : columns) {
                        totals.add(ReportsUtil.getTotalByBillReportCategorizedItems(bills, serviceName));
                        total100 = total100.add(ReportsUtil.getTotalByBillReportCategorizedItems(bills, serviceName));
                    }
                }

            } catch (Exception e) {
                request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                        "No patient bill found Or Service categories not set properly. Contact System Admin... !");
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + e.getMessage());
            }

            if (!Objects.equals(insuranceRate.getFlatFee(), BigDecimal.ZERO)) {
                insuranceFlatFee = insuranceRate.getFlatFee();
            }

            request.getSession().setAttribute("columns", columns);
            request.getSession().setAttribute("listOfAllServicesRevenue", listOfAllServicesRevenue);
            request.getSession().setAttribute("insurance", insurance);

            mav.addObject("columns", columns);
            mav.addObject("totals", totals);
            mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
            mav.addObject("resultMsg", "[" + insurance.getName() + "] Bill from " + startDateStr + " To " + endDateStr);
            mav.addObject("insuranceFlatFee", insuranceFlatFee);

            mav.addObject("insuranceRate", insuranceRate.getRate());
            mav.addObject("total100", total100);
        }

        if (request.getParameter("export") != null) {
            FileExporter.exportData(request, response, insurance, columns, listOfAllServicesRevenue);
        }
        return mav;
    }

    private List<String> getColumns(List<HopService> reportColumns) {

        List<String> columns = reportColumns.stream()
                .map(HopService::getName)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Columns here 2: " + columns);

        if (!columns.contains("IMAGING")) {
            columns.add("IMAGING");
        }
        if (!columns.contains("PROCEDURES")) {
            columns.add("PROCED.");
        }
        return columns;
    }
}
