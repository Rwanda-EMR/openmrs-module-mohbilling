<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>


<b class="boxHeader">Bill Payment</b>

<div class="box" style="clear: both;">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Service</td>
			<th class="columnHeader center">Qty</td>
			<th class="columnHeader center">PaidQty</td>
			<th class="columnHeader right">Unit Price (Rwf)</td>
			<th class="columnHeader right">Price (Rwf)</td>					
		</tr>
		<c:if test="${!empty paidItems}">
				<tr>
					<td class="columnHeader"><a href="patientBillPayment.form?consommationId=${consommation.consommationId}&ipCardNumber=${consommation.beneficiary.policyIdNumber}">View</a></td>
					<th class="columnHeader" style="font-size: 1em; font-weight: bold;" colspan="4"><center>Date: ${consommation.createdDate}, Total Amount : ${patientBill.amount} Rwf, Insurance : ${patientBill.beneficiary.insurancePolicy.insurance.name}, Policy Card No. : ${patientBill.beneficiary.policyIdNumber}</center></th>
					<td class="columnHeader">						
							<a href="refundBill.form?consommationId=${consommation.consommationId}&ipCardNumber=${consommation.beneficiary.policyIdNumber}">Refund</a>
					</td>
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
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${paidItem.billItem.unitPrice*paidItem.billItem.quantity}</td>			
				</tr>
			</c:forEach> 
	      </c:if>
	</table>
</div>

