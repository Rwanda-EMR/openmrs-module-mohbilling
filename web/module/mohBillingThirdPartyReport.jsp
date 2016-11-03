<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
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
.insurances,.time,.collector {
    display: none;
} 
a.print {
    border: 2px solid #009900;
    background-color: #aabbcc;
    color: #ffffff;
    text-decoration: none;
}
</style>

<h2>Third Party Report</h2>

<c:import url="mohBillingReportParameters.jsp" />

<c:if test="${empty listOfAllServicesRevenue }">
 <div style="text-align: center;color: red;"><p>No [Fiche de Consommation] found!</p></div>
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
<a href="#" class="print">PDF</a></b>
<div class="box">
<table style="width: 100%">
	<tr>
		<th class="columnHeader" style="width: 3%">#</th>
		<th class="columnHeader" style="width: 8%">Date</th>
		<th class="columnHeader">Card NUMBER </th>
		<th class="columnHeader">AGE</th>
		<th class="columnHeader">Gender</th>
		<th class="columnHeader" style="width: 15%">BENEFICIARY'S NAMES</th>
		<th class="columnHeader">CONSULTATION </th>
		<th class="columnHeader">LABO</th>
		<th class="columnHeader">IMAGING</th>
		<th class="columnHeader">HOSPIT.</th>
		<th class="columnHeader">PROC. & MAT.</th>
		<th class="columnHeader">OTHER CONSUMABLES</th>
		<th class="columnHeader">DRUGS</th>
		<th class="columnHeader">100%</th>
		<th class="columnHeader">Insurance: <b>${insuranceRate}%</b></th>
		<th class="columnHeader">Third Party: <b>${thirdPartyRate }%</b></th>
		<th class="columnHeader">Patient: <b>${patientRate}%</b></th>
	</tr>
    
	<c:set var="patientRate" value="${100-insuranceRate}"/>
	
	
	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<tr>	
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">
				<fmt:formatDate pattern="yyyy-MM-dd" value="${asr.consommation.createdDate}" />
            </td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.gender}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
			
			<c:set var="patientDueByConsom" value="0"/>
			
			<c:forEach items="${asr.revenues }" var="revenue">
			 <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${revenue.dueAmount}" type="number" pattern="#.##"/></td>
			 <c:set var="patientDueByConsom" value="${patientDueByConsom+revenue.dueAmount }"/>
			</c:forEach>
		
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${patientDueByConsom}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${patientDueByConsom*insuranceRate/100}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${patientDueByConsom*(patientRate-thirdPartyRate)/100}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${patientDueByConsom*thirdPartyRate/100}" type="number" pattern="#.##"/></td>
	    </tr>
	</c:forEach>
<tr>
<td><b style="color: blue;">TOTAL</b></td>
<td></td><td></td><td></td><td></td><td></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalConsult}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalLabo}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalImaging}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalHosp}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalProcAndMater}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalOtherCons}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${totalMedic}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*insuranceRate/100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*(patientRate-thirdPartyRate)/100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*thirdPartyRate/100}" type="number" pattern="#.##"/></b></td>
</tr>
	
	

</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>