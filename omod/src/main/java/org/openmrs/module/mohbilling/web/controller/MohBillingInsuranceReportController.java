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
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
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

        if (request.getParameter("formStatus") != null
                && !request.getParameter("formStatus").equals("")) {

            List<AllServicesReportRevenue> listOfAllServicesRevenue = new ArrayList<>();
            List<String> columns = new ArrayList<>();
            List<BigDecimal> totals = new ArrayList<BigDecimal>();
            BigDecimal total100 = BigDecimal.ZERO;
            BigDecimal insuranceFlatFee = BigDecimal.ZERO;

            Integer insuranceId = null;
            String insuranceStr = request.getParameter("insuranceId");
            if (insuranceStr != null && !insuranceStr.trim().isEmpty()) {
                insuranceId = Integer.valueOf(insuranceStr);
            }

            Insurance insurance = InsuranceUtil.getInsurance(insuranceId);
            InsuranceRate insuranceRate = insurance.getCurrentRate();
            request.getSession().setAttribute("insurance", insurance);

            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");

            Date startDate = null;
            Date endDate = null;

            if ((startDateStr != null && !startDateStr.trim().isEmpty()) &&
                    (endDateStr != null && !endDateStr.trim().isEmpty())) {

                DateFormat formatter;
                try {
                    formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    startDate = formatter.parse(startDateStr + " 00:00:00");
                    endDate = formatter.parse(endDateStr + " 23:59:59");
                } catch (ParseException | IllegalArgumentException | NullPointerException ex) {
                    System.err.println("Date parse Exception: " + ex.getMessage());
                } finally {
                    formatter = null;
                }
            }

            try {

                if (startDate != null && endDate != null && insuranceId != null) {

                    List<PatientServiceBillReport> patientBillsList = ReportsUtil.getPatientServiceBillReport(insuranceId, startDate, endDate);
                    Map<Integer, List<PatientServiceBillReport>> globalBillsMap = new HashMap<>();

                    for (PatientServiceBillReport bill : patientBillsList) {
                        globalBillsMap.computeIfAbsent(bill.getGlobalBillId(), k -> new LinkedList<>()).add(bill);
                    }
                    System.out.println("Patient Bills list size: " + patientBillsList.size());
                    System.out.println("Global Bills map size  : " + globalBillsMap.size());

                    List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.insuranceReportColumns");
                    columns.addAll(getColumns(reportColumns));

                    System.out.println("Columns here 1: " + columns);

                    for (Map.Entry<Integer, List<PatientServiceBillReport>> entry : globalBillsMap.entrySet()) {

                        List<ServiceReportRevenue> billRevenues = new ArrayList<>();
                        for (HopService hopService : reportColumns) {
                            billRevenues.add(ReportsUtil.getServiceReportRevenues(entry.getValue(), hopService, insurance));
                        }

                        ServiceReportRevenue imagingRevenue = ReportsUtil.getServiceReportRevenue(entry.getValue(), "mohbilling.IMAGING", insurance);
                        ServiceReportRevenue proceduresRevenue = ReportsUtil.getServiceReportRevenue(entry.getValue(), "mohbilling.PROCEDURES", insurance);
                        billRevenues.add(imagingRevenue);
                        billRevenues.add(proceduresRevenue);

                        BigDecimal allDueAmounts = BigDecimal.ZERO;
                        allDueAmounts = allDueAmounts.add(ReportsUtil.getTotalByBillReportItems(entry.getValue()));

                        PatientServiceBillReport initialReport = entry.getValue().get(0);

                        AllServicesReportRevenue servicesRevenue = new AllServicesReportRevenue(BigDecimal.ZERO, BigDecimal.ZERO, "2016-09-11");
                        servicesRevenue.setRevenues(billRevenues);
                        servicesRevenue.setAllDueAmounts(allDueAmounts);
                        servicesRevenue.setConsommation(initialReport);//get any, they have some similar values we want

                        listOfAllServicesRevenue.add(servicesRevenue);
                    }

                    for (String serviceName : columns) {
                        totals.add(ReportsUtil.getTotalByBillReportCategorizedItems(patientBillsList, serviceName));
                        total100 = total100.add(ReportsUtil.getTotalByBillReportCategorizedItems(patientBillsList, serviceName));
                    }
                }

            } catch (Exception e) {
                request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                        "No patient bill found Or Service categories not set properly. Contact System Admin... !");
                System.out.println("Error occurred generating Report: " + e.getMessage());
                e.printStackTrace();
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + e.getMessage());
            }

            if (!Objects.equals(insuranceRate.getFlatFee(), BigDecimal.ZERO)) {
                insuranceFlatFee = insuranceRate.getFlatFee();
            }

            request.getSession().setAttribute("columns", columns);
            request.getSession().setAttribute("listOfAllServicesRevenue", listOfAllServicesRevenue);

            mav.addObject("columns", columns);
            mav.addObject("totals", totals);
            mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
            mav.addObject("resultMsg", "[" + insurance.getName() + "] " +
                    "Bill from " + convertDate(startDate) + " To " + convertDate(endDate));
            mav.addObject("insuranceFlatFee", insuranceFlatFee);

            mav.addObject("insuranceRate", insuranceRate.getRate());
            mav.addObject("total100", total100);
        }

        if (request.getParameter("export") != null) {
            List<String> columns = (List<String>) request.getSession().getAttribute("columns");
            List<AllServicesReportRevenue> listOfAllServicesRevenue = (List<AllServicesReportRevenue>)
                    request.getSession().getAttribute("listOfAllServicesRevenue");
            Insurance insurance = (Insurance) request.getSession().getAttribute("insurance");
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
        System.out.println("Columns here 2 count: " + columns.size());

        if (!columns.contains("IMAGING")) {
            columns.add("IMAGING");
        }
        if (!columns.contains("PROCEDURES")) {
            columns.add("PROCED.");
        }
        return columns;
    }

    private String convertDate(Date date) {
        DateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(date);
        } catch (Exception ex) {
            System.err.println("Date parse Exception: " + ex.getMessage());
        } finally {
            dateFormat = null;
        }
        return "--";
    }
}
