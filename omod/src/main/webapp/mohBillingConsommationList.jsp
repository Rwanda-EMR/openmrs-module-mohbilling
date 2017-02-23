<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Consommations List</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<div style="text-align: left;"><a href="viewGlobalBill.form?globalBillId=${globalBill.globalBillId }">View Global Bill Details</a></div>
<div style="text-align: right;">

 <!--  see http://www.quackit.com/html/codes/html_close_window_code.cfm  -->
 <c:if test="${not globalBill.closed}">
 <a href="javascript:window.open('admission.form?globalBillId=${globalBill.globalBillId}&insurancePolicyId=${insurancePolicy.insurancePolicyId }&ipCardNumber=${insurancePolicy.insuranceCardNo}&discharge=true', 'dischargeWindow', 'height=300,width=450,left=100,top=100,resizable=yes,scrollbars=yes,toolbar=no,menubar=no,location=no,directories=no,status=yes').focus()">Dischage the Patient | </a>

<a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add Consommation</a></div>
</c:if>
<b class="boxHeader">Consommations List for Global Bill id # ${globalBill.billIdentifier}</b>
<div class="box">
	<table>
		<tr>
			<th>#.</th>
			<th>Consom ID</th>
			<th>Service</th>
			<th>created By</th>
			<th>card No</th>
			<th>Insurance due</th>
			<th>Third party due</th>
			<th>Patient due</th>
			<th>Paid Amount</th>
			<th>status</th>
			<th>Payment</th>
			<th></th>
		</tr>
		<c:forEach items="${consommations}" var="consommation" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.consommationId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.department.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.creator.personName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.beneficiary.policyIdNumber}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.insuranceBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.thirdPartyBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.patientBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidForPatientBill(consommation.consommationId)}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billingtag:consommationStatus(consommation.consommationId)}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">&nbsp;<a href="patientBillPayment.form?consommationId=${consommation.consommationId}">view</a></td>
				<c:if test="${empty consommation.patientBill.payments && not consommation.globalBill.closed }">
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="billing.form?consommationId=${consommation.consommationId}&departmentId=${consommation.department.departmentId}&insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&edit=true">Edit</a></td>
				<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="billing.form?consommationId=${consommation.consommationId}&departmentId=${consommation.department.departmentId}&insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&globalBillId=${consommation.globalBill.globalBillId}&addNew=true">Add Item</a></td> -->
				</c:if>
			</tr>
		</c:forEach>
		<tr>
		   <td></td>
		</tr>
			<tr>
			     <td><div style="text-align: left;">Total Due Amount</div></td>
			     <c:set var="totalDueAmount" value="${(globalBill.globalAmount)*((100-insurancePolicy.insurance.currentRate.rate)/100)}"/>
				<td><div class="amount"><fmt:formatNumber value="${totalDueAmount}" type="number" pattern="#.##"/></div></td>
				<td></td><td></td> <td></td><td></td><td></td>
				<td><div style="text-align: right;"><b>Total paid  Amount</b></div></td>
				<td><div class="amount">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</div></div></td>
			</tr>
	</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>