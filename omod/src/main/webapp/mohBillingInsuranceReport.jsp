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
.thirdParties,.timelabel,.time,.collector,.billCreator,.billStatus,.services,.deposit,.paymentType {
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


<b>Big Report ? </b> <a href="billingsessionControl.form" target="_blank" > Click here</a> for auto reflesh after running a report

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
<a href="insuranceReport.form?export=true">Excel</a></b>
<div class="box">
<table style="width: 100%">
	<tr>


	<th class="columnHeader" style="width: 3%">#</th>
		<th class="columnHeader" style="width: 3%">Admission Date</th>
        <th class="columnHeader" style="width: 3%">Closing Date</th>
		<th class="columnHeader">BENEFICIARY's NAMES'</th>
		<th class="columnHeader">HEAD HOUSEHOLD'S NAMES </th>
		<th class="columnHeader">FAMILY'S CODE </th>
		<th class="columnHeader">LEVEL </th>
		<th class="columnHeader" style="width: 6%">GB#</th>
		<th class="columnHeader" style="width: 6%">Card NUMBER</th>
		<th class="columnHeader">COMPANY</th>
		<th class="columnHeader">AGE</th>
		<th class="columnHeader">BIRTH DATE</th>
		<th class="columnHeader">Gender</th>
		<th class="columnHeader">DOCTOR</th>

		<c:forEach items="${columns }" var="categ">
			 <c:if test="${categ eq 'FORMALITES ADMINISTRATIVES' }">
				<th class="columnHeader">OTHERCONSUM. </th>
			 </c:if>
			 <c:if test="${categ != 'FORMALITES ADMINISTRATIVES' }">
				 <th class="columnHeader">${categ } </th>
			 </c:if>
		</c:forEach>

	</tr>

	<c:set var="patientRate" value="${100-insuranceRate}"/>
   <c:set var="totalFlatFee" value="0" scope="page" />

	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">

		<tr>

		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">
			<fmt:formatDate pattern="dd/MM/yyyy" value="${asr.admissionDate}" />
			</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">
            <fmt:formatDate pattern="dd/MM/yyyy" value="${asr.closingDate}" />
            </td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.beneficiaryName}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.householdHeadName}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.familyCode}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.beneficiaryLevel}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.globalBillIdentifier}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.cardNumber}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.companyName}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"> <fmt:formatDate pattern="dd/MM/yyyy" value="${asr.birthDate}" />  </td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.gender}</td>
            <td class="rowValue ${(status.count%2!=0)?'even':''}"> ${asr.doctorName}</td>

			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.medicament}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.consultation}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.hospitalisation}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.laboratoire}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.formaliteAdministratives}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.ambulance}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.consommables}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.oxygenotherapie}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.imaging}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.proced}" type="number" pattern="#.##"/></td>

			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.total100}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.totalInsurance}" type="number" pattern="#.##"/></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.totalPatient}" type="number" pattern="#.##"/></td>
	    </tr>
	</c:forEach>

<tr>
<td><b style="color: blue;">TOTAL</b></td>
<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td><td></td><td></td>
		<c:forEach items="${totals }" var="total">
		  <td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total}" type="number" pattern="#.##"/></b> </td>
		</c:forEach>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b> </td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*insuranceRate/100 - totalFlatFee}" type="number" pattern="#.##"/></b> </td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*patientRate/100 + totalFlatFee}" type="number" pattern="#.##"/></b> </td>
</tr>

</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>