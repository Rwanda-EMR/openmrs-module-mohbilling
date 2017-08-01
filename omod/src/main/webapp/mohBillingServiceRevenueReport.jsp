<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>

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
 .insurances, .thirdParties,.time,.collector,.billCreator,.billStatus,.deposit,.paymentType {
    display: none;
} 
a.print {
    border: 2px solid #009900;
    background-color: #aabbcc;
    color: #ffffff;
    text-decoration: none;
}
</style>

<h2>Service Revenue Report</h2>

<c:import url="mohBillingReportParameters.jsp" />

<c:if test="${empty departmentsRevenues }">
 <div style="text-align: center;color: red;"><p>No payments found!</p></div>
</c:if>

<c:if test="${not empty departmentsRevenues }">
<br/>
<b class="boxHeader">
${resultMsg } : <b style="color: black;font: bold;"><fmt:formatNumber value="${totalRevenueAmount}" type="number" pattern="#.##"/> FRW</b>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
<a href="serviceRevenueReport.form?print=true" class="print">PDF</a></b>
<div class="box">
<table style="width:70%">
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">SERVICE</th>	
		<c:forEach items="${services}" var="s" varStatus="status">
		<th class="columnHeader">${s.service}</th>	
		</c:forEach> 
		<th class="columnHeader">TOTAL</th>
	</tr>

	<c:forEach items="${departmentsRevenues}" var="dsr" varStatus="status">
	<tr class="rowValue ${(status.count%2!=0)?'even':''}">
	 <td>${status.count}.</td>
	 <td>${dsr.department.name}</td>
	  <c:forEach items="${dsr.paidServiceRevenues}" var="sr" varStatus="status">
		 <td><fmt:formatNumber value="${sr.paidAmount}" type="number" pattern="#.##"/></td>
	  </c:forEach>
	  <td><b><fmt:formatNumber value="${dsr.amount}" type="number" pattern="#.##"/></b></td>
	</tr>
	</c:forEach>
</table>

</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>