<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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

<h2>
    Provider Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />

<c:if test="${empty listOfAllServicesRevenue }">
    <div style="text-align: center;color: red;"><p>No Patient Bill found!</p></div>
</c:if>

<c:if test="${not empty listOfAllServicesRevenue }">
    <br/>
    <b class="boxHeader">
            ${resultMsg} <b style="color: black;font: bold;"></b>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="providerDCPReport.form?export=true">Excel</a></b>
    <div class="box">
        <table style="width: 100%">
            <tr>
                <th class="columnHeader" style="width: 3%">#</th>
                <th class="columnHeader">BENEFICIARY NAMES</th>
                <th class="columnHeader">INSURANCE NAME</th>
                <th class="columnHeader">INSURANCE NO</th>
                <th class="columnHeader">DATE CREATED</th>
                <th class="columnHeader">DOCTOR</th>
            <c:forEach items="${columns}" var="categ">
                <th class="columnHeader">${categ} </th>
            </c:forEach>
                <th class="columnHeader">100%</th>
                <th class="columnHeader">Insurance</th>
                <th class="columnHeader">Patient</th>
            </tr>
            <c:set var="patientRate" value="${100-insuranceRate}"/>
            <c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
                <tr>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insurance.name}</td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatDate pattern="dd/MM/yyyy" value="${asr.consommation.createdDate}"/></td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.creator}</td>
                <c:forEach items="${asr.revenues}" var="revenue">
                    <c:if test="${patientRate > 0}">
                        <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${revenue.dueAmount*100/patientRate}" type="number" pattern="#.##"/></td>
                    </c:if>
                    <c:if test="${patientRate==0}">
                        <c:set var="amount" value="0" />
                        <c:forEach items="${revenue.billItems}" var="item">
                            <c:set var="amount" value="${amount + (item.unitPrice)*(item.quantity)}"/>
                        </c:forEach>
                        <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${amount}" type="number" pattern="#.##"/></td>
                    </c:if>
                </c:forEach>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts}" type="number" pattern="#.##"/></td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.bigTotal}" type="number" pattern="#.##"/></td>
                    <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.patientTotal}" type="number" pattern="#.##"/></td>
                    <td class="rowTotalValue"><a href="patientBillPayment.form?consommationId=${asr.consommation.consommationId}">View/</a></td>
                </tr>
            </c:forEach>
            <tr>
                <td><b style="color: blue;">TOTAL</b></td>
                <td></td><td></td><td><td></td><td></td>
                <c:forEach items="${totals }" var="total">
                    <td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total}" type="number" pattern="#.##"/></b> </td>
                </c:forEach>
                <td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b> </td>
            </tr>
        </table>
    </div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>