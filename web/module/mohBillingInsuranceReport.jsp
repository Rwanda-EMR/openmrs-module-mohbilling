<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
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
.thirdParties,.time,.collector {
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
	 Insurance Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />

<c:if test="${empty listOfAllServicesRevenue }">
 <div style="text-align: center;color: red;"><p>No [Fiche de Consommation] found!</p></div>
</c:if>

<c:if test="${not empty listOfAllServicesRevenue }">
<br/>
<b class="boxHeader">
${resultMsg} : <b style="color: black;font: bold;">${globalBillAmount }FRW</b>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
<a href="#" class="print">PDF</a></b>
<div class="box">
<table>
	<tr>
		<th class="columnHeader">#</th>
		<th class="columnHeader">Date</th>
		<th class="columnHeader">Card NUMBER </th>
		<th class="columnHeader">AGE</th>
		<th class="columnHeader">Gender</th>
		<th class="columnHeader">BENEFICIARY'S NAMES</th>
		<th class="columnHeader">CONSULTATION </th>
		<th class="columnHeader">LABORATORY</th>
		<th class="columnHeader">RADIO</th>
		<th class="columnHeader">HOSPITALIZATION</th>
		<th class="columnHeader">PROCEDURES  AND MATERIALS</th>
		<th class="columnHeader">OTHER CONSUMABLES</th>
		<th class="columnHeader">DRUGS</th>
		<th class="columnHeader">Amount</th>
	</tr>
    
	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<tr>	
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.createdDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.gender}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
			<c:forEach items="${asr.revenues }" var="revenue">
			 
			 <td class="rowValue ${(status.count%2!=0)?'even':''}">${revenue.dueAmount }</td>
			 
			</c:forEach>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b>${asr.allDueAmounts }</b></td>
	    </tr>
	</c:forEach>

	
	

</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>