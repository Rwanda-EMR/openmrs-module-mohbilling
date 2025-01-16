package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingCohortBuilderFormController extends ParameterizableViewController {
    protected final Log log = LogFactory.getLog(getClass());
    private static final String STATUS_FULLY_PAID = "FULLY PAID";
    private static final String STATUS_PARTLY_PAID = "PARTLY PAID";
    private static final String STATUS_UNPAID = "UNPAID";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(getViewName());

        // Get pagination parameters
        int page = getPageParameter(request);
        int recordsPerPage = getRecordsPerPageParameter(request);

        // Reset the page to 1 if a new search is performed (i.e., "Search" button clicked)
        if (request.getMethod().equalsIgnoreCase("POST") && request.getParameter("submitId") != null) {
            page = 1;  // Reset to first page on new search
        }

        // Extract filter parameters
        User billCreator = getBillCreator(request);
        Department department = getDepartment(request);
        Insurance insurance = getInsurance(request);
        ThirdParty thirdParty = getThirdParty(request);
        String billStatus = request.getParameter("billStatus");

        // Dates
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");


        // Handle form validation and retrieve results
        if (isFormStatusValid(request)) {
            Date startDate = Utils.formatInputStringToDate(startDateStr);
            Date endDate = Utils.formatInputStringToDate(endDateStr);

            List<Consommation> consommations = getFilteredConsommations(startDate, endDate, insurance, thirdParty, billCreator, department, recordsPerPage, page, billStatus);
            int totalRecords = ConsommationUtil.getTotalConsommations(startDate, endDate, insurance, thirdParty, billCreator, department);

            mav.addObject("consommations", consommations);
            mav.addObject("totalRecords", totalRecords);
            mav.addObject("totalPages", calculateTotalPages(totalRecords, recordsPerPage));
        }

        // Add form data
        addFormDataToModel(mav, startDateStr, endDateStr, recordsPerPage, page, billCreator, billStatus, department);

        return mav;
    }

    // Utility Methods

    private int getPageParameter(HttpServletRequest request) {
        String currentPage = request.getParameter("pageNumber");
        return (currentPage == null || currentPage.isEmpty()) ? 1 : Integer.parseInt(currentPage);
    }

    private int getRecordsPerPageParameter(HttpServletRequest request) {
        String recordsPerPageStr = request.getParameter("pageSize");
        return (recordsPerPageStr == null || recordsPerPageStr.isEmpty()) ? 25 : Integer.parseInt(recordsPerPageStr);
    }

    private User getBillCreator(HttpServletRequest request) {
        String billCreatorStr = request.getParameter("billCreator");
        return (billCreatorStr != null && !billCreatorStr.isEmpty()) ? Context.getUserService().getUser(Integer.valueOf(billCreatorStr)) : null;
    }

    private Department getDepartment(HttpServletRequest request) {
        String departmentStr = request.getParameter("departmentId");
        return (departmentStr != null && !departmentStr.isEmpty()) ? DepartementUtil.getDepartement(Integer.valueOf(departmentStr)) : null;
    }

    private Insurance getInsurance(HttpServletRequest request) {
        String insuranceIdStr = request.getParameter("insuranceIdStr");
        return (insuranceIdStr != null && !insuranceIdStr.isEmpty()) ? InsuranceUtil.getInsurance(Integer.valueOf(insuranceIdStr)) : null;
    }

    private ThirdParty getThirdParty(HttpServletRequest request) {
        String thirdPartyIdStr = request.getParameter("thirdPartyIdStr");
        return (thirdPartyIdStr != null && !thirdPartyIdStr.isEmpty()) ? Context.getService(BillingService.class).getThirdParty(Integer.valueOf(thirdPartyIdStr)) : null;
    }

    private boolean isFormStatusValid(HttpServletRequest request) {
        String formStatus = request.getParameter("formStatus");
        return formStatus != null && !formStatus.isEmpty();
    }

    private List<Consommation> getFilteredConsommations(Date startDate, Date endDate, Insurance insurance, ThirdParty thirdParty, User billCreator, Department department, int recordsPerPage, int page, String billStatus) {
        List<Consommation> consommations = ConsommationUtil.getConsommations(startDate, endDate, insurance, thirdParty, billCreator, department, recordsPerPage, page);

        if (billStatus != null && !billStatus.isEmpty()) {
            return filterByStatus(consommations, billStatus);
        }
        return consommations;
    }

    private List<Consommation> filterByStatus(List<Consommation> consommations, String billStatus) {
        List<Consommation> filteredList = new ArrayList<>();

        for (Consommation con : consommations) {
            PatientBill patientBill = con.getPatientBill();
            BigDecimal totalPaidAmount = getTotalPaidAmount(patientBill);

            if (billStatus.equals(STATUS_FULLY_PAID) && isFullyPaid(patientBill, totalPaidAmount)) {
                filteredList.add(con);
            } else if (billStatus.equals(STATUS_PARTLY_PAID) && isPartlyPaid(patientBill, totalPaidAmount)) {
                filteredList.add(con);
            } else if (billStatus.equals(STATUS_UNPAID) && isUnpaid(patientBill)) {
                filteredList.add(con);
            }
        }

        return filteredList;
    }

    private BigDecimal getTotalPaidAmount(PatientBill patientBill) {
        return patientBill.getPayments().stream()
                .map(BillPayment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isFullyPaid(PatientBill patientBill, BigDecimal totalPaidAmount) {
        return patientBill.getAmount().compareTo(totalPaidAmount) <= 0;
    }

    private boolean isPartlyPaid(PatientBill patientBill, BigDecimal totalPaidAmount) {
        return patientBill.getAmount().compareTo(totalPaidAmount) > 0 && !patientBill.getPayments().isEmpty();
    }

    private boolean isUnpaid(PatientBill patientBill) {
        return patientBill.getPayments().isEmpty();
    }

    private void addFormDataToModel(ModelAndView mav, String startDateStr, String endDateStr, int recordsPerPage, int page, User billCreator, String billStatus, Department department) {
        mav.addObject("insurances", InsuranceUtil.getAllInsurances());
        mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());
        mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());
        mav.addObject("startDate", startDateStr);
        mav.addObject("endDate", endDateStr);
        mav.addObject("pageSize", recordsPerPage);
        mav.addObject("currentPage", page);
        mav.addObject("billCreator", billCreator != null ? billCreator.getUserId() : "");
        mav.addObject("billStatus", billStatus);
        mav.addObject("departmentId", department != null ? department.getDepartmentId() : "");
    }

    private int calculateTotalPages(int totalRecords, int recordsPerPage) {
        return totalRecords == 0 ? 0 : (int) Math.ceil((double) totalRecords / recordsPerPage);
    }
}