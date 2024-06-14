package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

            List<InsuranceReportItem> insuranceReportRecords = new ArrayList<>();
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
                    formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

                    InsuranceReport reportItems = ReportsUtil.getPatientServiceBillReport(insuranceId, startDate, endDate);
                    insuranceReportRecords = reportItems.getReportItems();

                    for (Map.Entry<String, BigDecimal> revenueItem : reportItems.getServiceTotalRevenues().entrySet()) {

                        System.out.println("Column Name : " + revenueItem.getKey());
                        System.out.println("Column Total: " + revenueItem.getValue());

                        columns.add(revenueItem.getKey());
                        totals.add(revenueItem.getValue());
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
            request.getSession().setAttribute("listOfAllServicesRevenue", insuranceReportRecords);

            mav.addObject("columns", columns);
            mav.addObject("totals", totals);
            mav.addObject("listOfAllServicesRevenue", insuranceReportRecords);
            mav.addObject("resultMsg", "[" + insurance.getName() + "] " +
                    "Bill from " + convertDate(startDate) + " To " + convertDate(endDate));
            mav.addObject("insuranceFlatFee", insuranceFlatFee);

            mav.addObject("insuranceRate", insuranceRate.getRate());
            mav.addObject("total100", total100);

            System.out.println("and now here...");
        }

        if (request.getParameter("export") != null) {
            List<String> columns = (List<String>) request.getSession().getAttribute("columns");
            List<InsuranceReportItem> insuranceReportRecords = (List<InsuranceReportItem>)
                    request.getSession()
                            .getAttribute("listOfAllServicesRevenue");
            Insurance insurance = (Insurance) request.getSession().getAttribute("insurance");
            FileExporter.exportData(response, insurance, insuranceReportRecords);
        }
        return mav;
    }

    private List<String> getColumns(List<HopService> reportColumns) {

        List<String> columns = reportColumns.stream()
                .map(HopService::getName)
                .distinct()
                .collect(Collectors.toList());

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