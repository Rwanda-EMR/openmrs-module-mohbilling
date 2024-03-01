<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingReportHeader.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>

<script type="text/javascript" language="JavaScript">

    var $bill = jQuery.noConflict();

    $bill(document).ready(function(){

        $bill('.meta').hide();

        $bill('#submitId').click(function() {

            $bill('#formStatusId').val("clicked");

        });

        $bill("input#print_button").click(function() {

            $bill('.meta').show();

            $bill("div.printarea").printArea();

            $bill('.meta').hide();

        });



    });

</script>




<style>

    .thirdParties,.timelabel,.time,.collector,.insurances,.billStatus,.services,.deposit,.paymentType,.reportType {

        display: none;

    }

    a.print {

        border: 2px solid #009900;

        background-color: #aabbcc;

        color: #ffffff;

        text-decoration: none;

    }

</style>




<h3>

    DCP Provider Report

</h3>




<c:import url="mohBillingReportParameters.jsp" />







<c:if test="${empty consommations }">

    <div style="text-align: center;color: red;"><p>No Data found!</p></div>

</c:if>




<c:if test="${not empty consommations }">

    <br/>

    <b class="boxHeader">

            ${resultMsg} <b style="color: black;font: bold;"></b>

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;







        <a href="providerDCPReport.form?export=true">Excel</a></b>

    <div class="box">

        <table width="99%">

            <tr>

                <td>No</td>

                <td>Date</td>

                <td>Department</td>

                <td> Creator</td>

                <td>Policy Id Number</td>

                <td>Beneficiary</td>




                <td>Insurance Name</td>

                <td>Total</td>

                <td>Insurance due</td>

                <td>Patient Due</td>

                <td>Admission Type</td>

            </tr>

            <c:set var="totalAmountAllConsom" value="0"/>

            <c:set var="totalAmountPaidAllConsom" value="0"/>

            <c:set var="totalInsurances" value="0"/>

            <c:set var="totalPatients" value="0"/>

            <c:forEach items="${consommations}" var="c" varStatus="status">




            <c:set var="insuranceRate" value="${(c.beneficiary.insurancePolicy.insurance.currentRate.rate)/100 }"/>

            <c:set var="patientRate" value="${(100-c.beneficiary.insurancePolicy.insurance.currentRate.rate)/100}"/>

            <c:set var="totalAmountByConsom" value="0"/>

            <c:forEach items="${c.billItems}" var="item" varStatus="status">

                <c:if test="${not item.voided}">

                <c:set var="totalAmountByConsom" value="${totalAmountByConsom+(item.unitPrice*item.paidQuantity)}" />

                </c:if>

            </c:forEach>

                <tr>

                    <td class="rowValue">${status.count}</td>

                    <td class="rowValue">

                        <fmt:formatDate pattern="yyyy-MM-dd" value="${c.createdDate}" />

                    </td>

                    <td class="rowValue">${c.department.name}</td>

                    <td class="rowValue">${c.creator.person.familyName}&nbsp;${c.creator.person.givenName}</td>

                    <td class="rowValue"><b>${c.beneficiary.policyIdNumber}</b></td>

                    <td class="rowValue">${c.beneficiary.patient.personName}</td>

                    <td class="rowValue">${c.beneficiary.insurancePolicy.insurance.name}</td>

                    <td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom}" type="number" pattern="#.##"/></td>

                    <td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom*insuranceRate }" type="number" pattern="#.##"/></td>

                    <td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom*patientRate }" type="number" pattern="#.##"/></td>

                    <c:if test="${c.globalBill.admission.isAdmitted==true}">

                        <td class="rowAmountValue" style="color: blue; font-weight: bold;">In-Patient</td>

                    </c:if>

                        <td class="rowAmountValue" style="color: blue; font-weight: bold;">${(c.globalBill.admission.admissionType=='2')?'DCP':'Ordinary'}</td>

                    <td class="rowTotalValue"><a href="patientBillPayment.form?consommationId=${c.consommationId}">View/</a></td>

                </tr>

                <c:set var="totalAmountAllConsom" value="${totalAmountAllConsom+totalAmountByConsom}" />

                <c:set var="totalAmountPaidAllConsom" value="${totalAmountPaidAllConsom+totalAmountPaidByCons}" />

                <c:set var="totalInsurances" value="${totalInsurances+(totalAmountByConsom*insuranceRate)}" />

                <c:set var="totalPatients" value="${totalPatients+(totalAmountByConsom*patientRate)}" />

            </c:forEach>

            <tr>

                <td class="rowTotalValue" colspan="7"><b style="color: blue;font-size: 14px;">TOTAL</b></td>

                <td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalAmountAllConsom}" type="number" pattern="#.##"/></b></td>

                <td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalInsurances}" type="number" pattern="#.##"/></b></td>

                <td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalPatients}" type="number" pattern="#.##"/></b></td>

                <td class="rowTotalValue"><b style="color: blue;font-size: 14px;"></b></td>

            </tr>

        </table>

    </div>

</c:if>

