<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>

<c:if test="${not payment.voided }">
<div style="float: right;"><a href="searchBillPayment.form?paymentId=${payment.billPaymentId}&consommationId=${consommation.consommationId}&print=true">Print Payment</a></div>
</c:if>
<br/>
<c:if test="${not payment.voided }">
<b class="boxHeader">Bill Payment(Amount Paid)</b>

<div class="box" style="clear: both;">
<form action="patientBillPayment.form?consommationId=${consommation.consommationId}&ipCardNumber=${param.ipCardNumber}&save=true" method="post" id="formSaveBillPayment">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Service</td>
			<th class="columnHeader center">Requested Qty</td>
			<th class="columnHeader center">Paid Qty</td>
			<th class="columnHeader right">Unit Price (Rwf)</td>
			<th class="columnHeader right">Price (Rwf)</td>
		</tr>
		<c:if test="${!empty paidItems}">
				<tr>
					<th class="columnHeader" style="font-size: 1em; font-weight: bold;" colspan="4"><center>Date: ${consommation.createdDate}, Total Amount : ${payment.amountPaid} Rwf, Insurance : ${consommation.beneficiary.insurancePolicy.insurance.name}, Policy Card No. : ${consommation.beneficiary.policyIdNumber}</center></th>
					<th class="columnHeader"></th>
					<th class="columnHeader"></th>
				</tr>
       </c:if>

		<c:if test="${!empty paidItems}">
		  	<c:forEach items="${paidItems}" var="paidItem" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${paidItem.billItem.service.facilityServicePrice.name}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${paidItem.billItem.quantity}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${paidItem.paidQty}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${paidItem.billItem.unitPrice}</td>
					<c:if test="${empty paidItem.paidQty }">
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${paidItem.billItem.unitPrice*paidItem.billItem.quantity}</td>
					</c:if>
					<c:if test="${not empty paidItem.paidQty }">
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${paidItem.billItem.unitPrice*paidItem.paidQty}</td>
					</c:if>
				</tr>
			</c:forEach>
	      </c:if>
	      <tr style="font-size: 1em">
				<td><div style="text-align: right;"><b>Enter New Amount</b></div></td>
				<td><input type="text" name="newAmount"/></td>
				<td><div style="text-align: right;"><b>Amount Paid</b></div></td>
				<td><div class="amount">${payment.amountPaid}</div></td>
				<td><input type="hidden" name="paymentId" value="${payment.billPaymentId }"/></td>
			</tr>
			<tr style="font-size: 1.2em">

					<td colspan="2"><input type="submit"  value="Save" style="min-width: 200px;" class="submitBtn"/></td>

			 <td colspan="3"></td>
	</table>
	</form>
</div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>