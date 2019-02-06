<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
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
.insurances, .thirdParties,.billCreator,.billStatus,.services,.deposit {
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
	Detailed Cashier Daily Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />
<openmrs:hasPrivilege privilege="Migration Cashier">
<a href="beforeMigrationcashierReport.form">Click here</a> for summarized report
</openmrs:hasPrivilege>
<div>

<c:if test="${empty paymentRevenues }">
 <div style="text-align: center;color: red;"><p>No Data!</p></div>
</c:if>

<c:if test="${not empty paymentRevenues }">
<br/>
<b class="boxHeader">
${resultMsg }(Paid): <b style="color: black;font: bold;"><fmt:formatNumber value="${totalRevenueAmount}" type="number" pattern="#.##"/> FRW</b>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
<a href="cashierReport.form?print=true" class="print">PDF</a></b>
<div class="box">
<table style="width:70%">
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">DATE</th>
		<th class="columnHeader">Patient Names</th>
		<c:forEach items="${services}" var="s" varStatus="status">
		<th class="columnHeader">${s.service}</th>	
		</c:forEach> 
		<th class="columnHeader"><b>TOTAL Due</b></th>
		<th class="columnHeader"><b>TOTAL Paid</b></th>
	</tr>

 	<c:forEach items="${paymentRevenues}" var="pr" varStatus="status">
	<tr class="rowValue ${(status.count%2!=0)?'even':''}">
	 <td>${status.count}.</td>
	 <td>${pr.payment.dateReceived}</td>
	 <td>${pr.beneficiary.patient.personName }</td>
	  <c:forEach items="${pr.paidServiceRevenues}" var="sr" varStatus="status">
		 <td><fmt:formatNumber value="${sr.paidAmount}" type="number" pattern="#.##"/></td>
	  </c:forEach>
	  <td><b><fmt:formatNumber value="${pr.amount}" type="number" pattern="#.##"/></b></td>
	 <c:set var="pt" value="${pr.payment}"/>
	  <td><b><fmt:formatNumber value="${pt.amountPaid}" type="number" pattern="#.##"/></b></td>
	</tr>
	</c:forEach> 
	<tr>
	<td class="rowValue"><b>TOT(Due)</b></td>
	<td class="rowValue"></td><td class="rowValue"></td>
	<c:forEach items="${subTotals}" var="st" varStatus="status">
		<td class="rowValue"><b><fmt:formatNumber value="${st}" type="number" pattern="#.##"/></b></td>
		</c:forEach> 
		<td class="rowValue"><b><fmt:formatNumber value="${bigTotal}" type="number" pattern="#.##"/></b></td>
		<td class="rowValue"><b><fmt:formatNumber value="${totalRevenueAmount}" type="number" pattern="#.##"/></b></td>
	</tr>
</table>

</div>
</c:if>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>