
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
.insurances,.time,.collector,.billCreator,.billStatus,.services,.deposit,.paymentType{
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

		<c:forEach items="${columns }" var="categ">
		 <c:if test="${categ eq 'FORMALITES ADMINISTRATIVES' }">
		  <th class="columnHeader">OTHERCONSUM. </th>
		 </c:if>
		 <c:if test="${categ != 'FORMALITES ADMINISTRATIVES' }">
		  		<th class="columnHeader">${categ } </th>
		 </c:if>
		</c:forEach>
     <c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<th class="columnHeader">100%</th>
		<th class="columnHeader">Insurance: <b>${asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate}%</b></th>
		<th class="columnHeader">Third Party: <b>${thirdPartyRate }%</b></th>
		<th class="columnHeader">Patient: <b>${100-asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate}%</b></th>
	</tr>
	<c:set var="patientRate" value="${100-asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate}"/>
</c:forEach>

	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<tr>
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">
				<fmt:formatDate pattern="yyyy-MM-dd" value="${asr.consommation.createdDate}" />
            </td>
            <!-- <td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insurance.name}</td> -->

			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>

			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.gender}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>


			<c:forEach items="${asr.revenues }" var="revenue">
			 <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${revenue.dueAmount*100/patientRate}" type="number" pattern="#.##"/></td>
			</c:forEach>

		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts*asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate/100}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts*thirdPartyRate/100}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts*(100-asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate-thirdPartyRate)/100}" type="number" pattern="#.##"/></td>


	    </tr>
	</c:forEach>
<tr>
   <td><b style="color: blue;">TOTAL</b></td>
   <td></td><td></td><td></td><td></td><td></td>
		<c:forEach items="${totals }" var="total">
		  <td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total}" type="number" pattern="#.##"/></b> </td>
		</c:forEach>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*(100-asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate-thirdPartyRate)/100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*thirdPartyRate/100}" type="number" pattern="#.##"/></b></td>
<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*asr.consommation.beneficiary.insurancePolicy.insurance.currentRate.rate/100}" type="number" pattern="#.##"/></b></td>

</tr>

</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>