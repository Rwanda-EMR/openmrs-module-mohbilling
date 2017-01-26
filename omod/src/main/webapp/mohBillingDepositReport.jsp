<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<script type="text/javascript">
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
.insurances, .thirdParties,.time,.billCreator,.billStatus,.services,.paymentType {
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
	Deposits/Caution Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />

<c:if test="${empty transactions }">
 <div style="text-align: center;color: red;"><p>No Transaction found!</p></div>
</c:if>

<c:if test="${not empty transactions }">
<br/>
<b class="boxHeader">
<c:if test="${reason=='Deposit' }">
Total Deposits Collected : 
</c:if>
<c:if test="${reason=='Bill Payment' }">
Payments Made With Patients' Deposits : 
</c:if>
<c:if test="${reason=='Withdrawal' }">
Amount Withdrawn : 
</c:if>
<b style="color: black;font: bold;">${total < 0 ? -total:total} FRW</b>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
<a href="depositReport.form?printPdf=true" class="print">PDF</a></b>
<div class="box">
<table style="width:70%">
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">Date</th>
		<th class="columnHeader">Collector</th>
		<th class="columnHeader">Patient Names</th>	
		<th class="columnHeader">Amount</th>
		<th class="columnHeader">Reason</th>
	</tr>

	<c:forEach items="${transactions}" var="trans" varStatus="status">
	<tr>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">
			<fmt:formatDate pattern="yyyy-MM-dd" value="${trans.transactionDate }" />
	</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}"> ${trans.collector }</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}"> ${trans.patientAccount.patient.personName }</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.amount < 0 ? -trans.amount:trans.amount}</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.reason}</td>
	</tr>
	</c:forEach>
</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>