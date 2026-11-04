<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<openmrs:require privilege="Billing Report - View Bill Insurance Reconciliation" otherwise="/login.htm" redirect="/module/@MODULE_ID@/billInsuranceReconciliation.form" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<style>
.reconHints {
	background: #fff8e1;
	border: 1px solid #f0c36d;
	padding: 10px 14px;
	margin: 12px 0;
}
.reconHints ul {
	margin: 6px 0 0 18px;
	padding: 0;
}
.reconHints li {
	margin-bottom: 4px;
}
</style>

<script type="text/javascript" language="JavaScript">
	var $bill = jQuery.noConflict();
	$bill(document).ready(function(){
		$bill('#submitId').click(function() {
			$bill('#formStatusId').val("clicked");
		});
	});
</script>

<h2>Bill Report vs Insurance Report</h2>

<div class="reconHints">
	<b>Hints</b>
	<ul>
		<li>This report lists global bills that appear in <b>Bill Report</b> or <b>Insurance Report</b> for the selected admission dates, but not in both.</li>
		<li>The <b>Appears In</b> column shows which report currently has the bill.</li>
		<li>Insurance Report includes only <b>closed</b> global bills. Open admissions often appear as Bill Report only with reason &quot;not closed&quot;.</li>
		<li>Both reports read ETL caches. If results look stale, run/wait for Bill Report ETL and Insurance Report ETL.</li>
		<li>Bill Report may exclude admissions before its configured ETL start date.</li>
		<li>Bills for the insurance named in <code>rwandaemr.autoCreateInsuranceType</code> (often private / NONE) are excluded, because that insurance usually has no Insurance Report rows.</li>
	</ul>
</div>

<b class="boxHeader">Search Form</b>
<div class="box">
	<form method="post" action="">
		<input type="hidden" name="formStatus" id="formStatusId" value="" />
		<table>
			<tr>
				<td>Start Date:</td>
				<td>
					<input type="text" name="startDate" size="11" value="${startDate}" onclick="showCalendar(this)" />
				</td>
				<td>End Date:</td>
				<td>
					<input type="text" name="endDate" size="11" value="${endDate}" onclick="showCalendar(this)" />
				</td>
				<td>
					<input type="submit" id="submitId" value="Search" />
				</td>
			</tr>
		</table>
	</form>
</div>

<c:if test="${not empty resultMsg}">
<br/>
<b class="boxHeader">
${resultMsg}
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="billInsuranceReconciliation.form?export=true&amp;startDate=${startDate}&amp;endDate=${endDate}">Excel</a>
</b>
<div class="box">
	<table style="width: 100%">
		<tr>
			<th class="columnHeader">#</th>
			<th class="columnHeader">Appears In</th>
			<th class="columnHeader">Reason</th>
			<th class="columnHeader">Voucher ID</th>
			<th class="columnHeader">Global Bill ID</th>
			<th class="columnHeader">Admission Date</th>
			<th class="columnHeader">Closing / Discharge Date</th>
			<th class="columnHeader">Patient</th>
			<th class="columnHeader">Patient ID</th>
			<th class="columnHeader">Card No</th>
			<th class="columnHeader">Insurance</th>
			<th class="columnHeader">Total</th>
		</tr>
		<c:forEach items="${rows}" var="row" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.appearsIn}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.reason}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.voucherId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.globalBillId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${row.admissionDate}" />
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${row.closingDate}" />
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientNames}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.cardNumber}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.insuranceName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.totalAmount}</td>
			</tr>
		</c:forEach>
		<c:if test="${empty rows}">
			<tr>
				<td colspan="12" class="rowValue">No discrepancies found for this date range.</td>
			</tr>
		</c:if>
	</table>
</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
