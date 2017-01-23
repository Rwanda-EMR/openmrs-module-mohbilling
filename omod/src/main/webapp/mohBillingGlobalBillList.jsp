<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<style>

	.closedStutus{
		color: #FFFFFF;
		background-color: red;
		padding: 2px;
		cursor: pointer;
		-moz-border-radius: 2px; 
		border-right: 2px solid #dddddd;
		border-bottom: 2px solid #dddddd;
	}

</style>

<h2>Global Bill List</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<br />

<b class="boxHeader">Global Bill  List </b>
<div class="box">
	<table>
		<tr>
		<th>#.</th>		
		<th>Date of Bill</th>
		<th>Created By</th>		
		<th>Policy ID No.</th>
		<th>Admission Date</th>
		<th>Discharging Date</th>		
		<th>Bill identifier</th>
		<th>Patient Due Amount</th>
		<th>Paid Amount</th>	
		<th>Status</th>	
		<th>Bill</th>					
	</tr>
		<c:forEach items="${globalBills}" var="globalBill" varStatus="status">
	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="globalBillId" value="${globalBill.globalBillId}" />
	<c:set var="insurancePolicyId" value="${globalBill.admission.insurancePolicy.insurancePolicyId}" />
	<c:set var="billIdentifier" value="${globalBill.billIdentifier}" />
	<c:set var="admissionDate" value="${globalBill.admission.admissionDate}" />
	<c:set var="dischargingDate" value="${globalBill.closingDate}"/>
	<c:set var="insuranceRate" value="${globalBill.admission.insurancePolicy.insurance.currentRate.rate}" />
	<c:set var="patientRate" value="${100-(globalBill.admission.insurancePolicy.insurance.currentRate.rate)}" />				
		<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${globalBill.createdDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${globalBill.creator.personName}</td>							
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${ipCardNumber}</td>
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${admissionDate}</td>
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${dischargingDate}</td>			
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billIdentifier}</td>			
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${(globalBill.globalAmount*patientRate)/100}</td>			
		    <td class="rowValue right ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</td>
		    <c:if test="${globalBill.closed}">	
		    <td class="rowValue center ${(status.count%2!=0)?'even':''}"><span title='Closed' class='closedStutus'><b>X</b></span></td>	
		    </c:if>
		    <c:if test="${not globalBill.closed}">	
		    <td class="rowValue center ${(status.count%2!=0)?'even':''}"><img src="/openmrs/images/edit.gif" title="Open" border="0" /></td>	
		    </c:if>
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">&nbsp;
		   
		    <c:if test="${not globalBill.closed}">
		    <a href="billing.form?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">Add</a>
		    </c:if>
		    <a href="consommation.list?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">View</a>
		    </td>
		 </tr>
		 
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>