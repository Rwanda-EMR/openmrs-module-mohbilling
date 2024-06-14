<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:require privilege="Manage Billing Reports" otherwise="/login.htm" redirect="/module/@MODULE_ID@/cohort.form" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<meta http-equiv="refresh" content="60" />
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
.collector,.time,.deposit,.timelabel,.paymentType {
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
	Patient Bills
</h3>

<c:if test="${empty consommations }">
 <div style="text-align: center;color: red;"><p>No bill found!</p></div>
</c:if>

<c:if test="${not empty consommations }">
<br/>
<b class="boxHeader">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;


<a href="cohort.form?print=true" class="print">PDF</a></b>
<div class="box">
<table width="99%">
	<tr>
		<td>No</td>
		<td>Date</td>
		<td>Department</td>
		<td>Creator</td>
		<td>Policy Id Number</td>
		<td>Beneficiary</td>
		<td>Insurance Name</td>
		<td>Total</td>
		<td>Insurance due</td>
		<td>Patient Due</td>
		<td>Paid Amount</td>
		<td>Phone number</td>
		<td>Ref. ID</td>
		<td>Bill Status</td>
		<td>Payment</td>
		<td>View Bill</td>

	</tr>
	<c:set var="totalAmountAllConsom" value="0"/>
	<c:set var="totalAmountPaidAllConsom" value="0"/>
	<c:set var="totalInsurances" value="0"/>
	<c:set var="totalPatients" value="0"/>
	<c:forEach items="${consommations}" var="c" varStatus="status">
		<tr>
		<c:set var="totalAmountByConsom" value="${c.insuranceBill.amount + c.patientBill.amount}" />
		<c:set var="totalAmountPaidByCons" value="${c.patientBill.getAmountPaid()}" />
		<c:set var="totalPatient" value="${c.patientBill.amount}" />
		<c:set var="totalInsurance" value="${c.insuranceBill.amount}" />
			<td class="rowValue">${status.count}</td>
			<td class="rowValue">
			<fmt:formatDate pattern="yyyy-MM-dd" value="${c.createdDate}" />
			</td>
			<td class="rowValue">${c.department.name}</td>
			<td class="rowValue">${c.creator.person.familyName}&nbsp;${c.creator.person.givenName}</td>
			<td class="rowValue"><b>${c.beneficiary.policyIdNumber}</b></td>
			<td class="rowValue">${c.beneficiary.patient.personName}</td>


			<td class="rowValue">${c.beneficiary.insurancePolicy.insurance.name}</td>
			<td class="rowAmountValue"><fmt:formatNumber value="${c.insuranceBill.amount + c.patientBill.amount }" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${c.insuranceBill.amount }" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${c.patientBill.amount }" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${c.patientBill.getAmountPaid() }" type="number" pattern="#.##"/></td>

	        <td class="rowAmountValue" style="color: green; font-weight: bold;">${c.patientBill.phoneNumber}</td>
		    <td class="rowAmountValue" style="color: green; font-weight: bold;">${c.patientBill.referenceId}</td>
			<td class="rowAmountValue" style="color: green; font-weight: bold;">${c.patientBill.transactionStatus}</td>
<td  class="rowValue">
<c:if test="${c.patientBill.transactionStatus=='SUCCESSFUL'}">
<form action="openConfirmationPage.form?consommationId=${c.consommationId}&startDate=${startDate}&endDate=${endDate}" method="post">
<input type="submit"  value="Confirm Payment" style="min-width: 100px;" class="submitBtn"/>
</form>
</c:if>
<c:if test="${c.patientBill.transactionStatus=='FAILED'}">
<form action="openConfirmationPage.form?patientBillId=${c.patientBill.patientBillId}&startDate=${startDate}&endDate=${endDate}" method="post">
<input type="text" name="phoneNumber" placeholder="Phone number" title="Phone number" size="8" />
<input type="submit"  value="Request to Pay" name="requesttopay" style="min-width: 50px;" class="submitBtn"/>
</form>
</c:if>
</td>




			<td class="rowTotalValue"><a href="patientBillPayment.form?consommationId=${c.consommationId}">View/</a></td>




		</tr>
	<c:set var="totalAmountAllConsom" value="${totalAmountAllConsom+totalAmountByConsom}" />
	<c:set var="totalAmountPaidAllConsom" value="${totalAmountPaidAllConsom+totalAmountPaidByCons}" />
	<c:set var="totalInsurances" value="${totalInsurances + totalInsurance}" />
	<c:set var="totalPatients" value="${totalPatients + totalPatient}" />
	</c:forEach>
	<tr>
		<td class="rowTotalValue" colspan="7"><b style="color: blue;font-size: 14px;">TOTAL</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalAmountAllConsom}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalInsurances}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalPatients}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalAmountPaidAllConsom}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"></b></td>
		<td class="rowTotalValue"><b style="color: red;font-size: 14px;"></b></td>
	</tr>
</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>