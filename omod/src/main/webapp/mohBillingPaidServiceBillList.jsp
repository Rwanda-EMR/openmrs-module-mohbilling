<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<c:if test="${not payment.voided }">
<div style="float: right;"><a href="searchBillPayment.form?paymentId=${payment.billPaymentId}&consommationId=${consommation.consommationId}&print=true">Print Payment</a></div>
</c:if>
<br/>
<c:if test="${not payment.voided }">
<b class="boxHeader">Bill Payment</b>

<div class="box" style="clear: both;">
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
					<!-- <td class="columnHeader"><a href="patientBillPayment.form?consommationId=${consommation.consommationId}&ipCardNumber=${consommation.beneficiary.policyIdNumber}">View</a></td> -->
					<td class="columnHeader">
                     <a href="patientBillPayment.form?consommationId=${consommation.consommationId}&ipCardNumber=${consommation.beneficiary.policyIdNumber}">View</a>
                     <openmrs:hasPrivilege privilege="Edit Bill Payment">
                     |
                     <a href="searchBillPayment.form?paymentId=${payment.billPaymentId}&consommationId=${consommation.consommationId}&editPay=true">Edit</a>
                     </openmrs:hasPrivilege>
                     </td>

					<th class="columnHeader" style="font-size: 1em; font-weight: bold;" colspan="4"><center>Date: ${consommation.createdDate}, Total Amount : ${payment.amountPaid} Rwf, Insurance : ${consommation.beneficiary.insurancePolicy.insurance.name}, Policy Card No. : ${consommation.beneficiary.policyIdNumber}</center></th>
					
					<openmrs:hasPrivilege privilege="Submit a refund">
					<td class="columnHeader">						
							<a href="paymentRefund.form?paymentId=${payment.billPaymentId}&ipCardNumber=${consommation.beneficiary.policyIdNumber}">Refund</a>
					</td>
					</openmrs:hasPrivilege>
					
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
	</table>
</div>
</c:if>

