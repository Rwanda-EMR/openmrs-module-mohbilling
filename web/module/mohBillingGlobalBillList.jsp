<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2>Global Bill Management</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>
<b class="boxHeader">Global Bill List</b>
<div class="box">
<table>
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">Date of Bill</th>		
		<th class="columnHeader">Policy ID No.</th>
		<th class="columnHeader">Bill identifier</th>
		<th class="columnHeader center">Due Amount</th>
		<th class="columnHeader right">paid Amount</th>		
		<th class="columnHeader right">Bill</th>					
	</tr>
	<c:forEach items="${globalBills}" var="globalBill" varStatus="status">
	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="globalBillId" value="${globalBill.globalBillId}" />
	<c:set var="insurancePolicyId" value="${globalBill.admission.insurancePolicy.insurancePolicyId}" />
	<c:set var="billIdentifier" value="${globalBill.billIdentifier}" />			
		<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>	
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${globalBill.createdDate}</td>			
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${ipCardNumber}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${billIdentifier}</td>			
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${globalBill.globalAmount}</td>			
		    <td class="rowValue right ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</td>		
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">&nbsp;<a href="billing.form?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">Add</a>
		    /<a href="consommation.list?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">View</a></td>
		 </tr>
		 
	</c:forEach>
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>