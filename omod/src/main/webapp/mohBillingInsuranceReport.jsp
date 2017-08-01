<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
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
.thirdParties,.time,.collector,.billCreator,.billStatus,.services,.deposit,.paymentType {
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
		<th class="columnHeader" style="width: 3%">Date</th>
		<th class="columnHeader" style="width: 6%">GB#</th>
		<th class="columnHeader" style="width: 6%">Card NUMBER</th>
		<th class="columnHeader">AGE</th>
		<th class="columnHeader">Gender</th>
		<th class="columnHeader">BENEFICIARY's NAMES'</th>

		<c:forEach items="${columns }" var="categ">
        		 <c:if test="${categ eq 'FORMALITES ADMINISTRATIVES' }">
        		  <th class="columnHeader">OTHERCONSUM. </th>
        		 </c:if>
		 <c:if test="${categ != 'FORMALITES ADMINISTRATIVES' }">
		  		<th class="columnHeader">${categ } </th>
		 </c:if>

		</c:forEach>
	<!--	<th class="columnHeader">Acts</th> -->
		<th class="columnHeader">100%</th>
		<th class="columnHeader">Insurance:<b>${insuranceRate }%</b></th>
		<th class="columnHeader">Patient:<b>${100-insuranceRate}%</b></th>
	</tr>
    
	<c:set var="patientRate" value="${100-insuranceRate}"/>
	
	
	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">

		<tr>

		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">
			<fmt:formatDate pattern="yyyy-MM-dd" value="${asr.consommation.createdDate}" />
			</td>

			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.globalBill.billIdentifier}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.gender}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
			
			<c:forEach items="${asr.revenues }" var="revenue">
				<c:if test="${patientRate > 0}">
			 		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${revenue.dueAmount*100/patientRate}" type="number" pattern="#.##"/></td>
				</c:if>
				<c:if test="${patientRate==0}">
					<c:set var="amount" value="0" />
					<c:forEach items="${revenue.billItems}" var="item">
						<c:set var="amount" value="${amount + item.service.maximaToPay}" />
					</c:forEach>
                 	<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${amount}" type="number" pattern="#.##"/></td>
                 </c:if>
			</c:forEach>
		 <td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts*insuranceRate/100}" type="number" pattern="#.##"/></td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${asr.allDueAmounts*patientRate/100}" type="number" pattern="#.##"/></td>
	    </tr>
	</c:forEach>
	
<tr>
<td><b style="color: blue;">TOTAL</b></td>
<td></td><td></td><td></td><td></td><td></td><td></td>
		<c:forEach items="${totals }" var="total">
		  <td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total}" type="number" pattern="#.##"/></b> </td>
		</c:forEach>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b> </td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*insuranceRate/100}" type="number" pattern="#.##"/></b> </td>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;"><fmt:formatNumber value="${total100*patientRate/100}" type="number" pattern="#.##"/></b> </td>
</tr>	
	

</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>