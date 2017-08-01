<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


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

<c:set var="insurancePolicy" value="${consommation.beneficiary.insurancePolicy}"/>
<c:set var="globalBill" value="${consommation.globalBill}"/>
    
<h2>Refunded Items </h2>

<div class="box">
<div>
<b class="boxHeader">Cashier Details</b>
<table>
<tr><td>Submitted By:</td><td><b>${refund.creator.personName }</b></td></tr>
<tr><td>Submitted On:</td><td><b>${refund.createdDate }</b></td></tr>
</table>
</div>
<div>
<b class="boxHeader">Patient Details</b>
<table>
<tr><td>Patient:</td><td>${insurancePolicy.owner.personName}</td></tr>
<tr><td>Insurance Policy:</td><td>${consommation.beneficiary.policyIdNumber}</td></tr>
</table>
</div>
</div>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div style="text-align: right;">

<br/>
<div class="box">
	<table width="99%">
			<tr>
				<th class="columnHeader"></th>
				<th class="columnHeader">Service</th>
				<th class="columnHeader">Qty Paid</th>
				<th class="columnHeader">Refund Qty</th>
				<th class="columnHeader">Unit Price (Rwf)</th>
				<th class="columnHeader">Reason</th>
				<th class="columnHeader">Action</th>
				<th></th>
			</tr>
			

			<c:forEach items="${refund.refundedItems}" var="refundItem" varStatus="status">
			<c:set var="psb" value="${refundItem.paidItem.billItem}"/>
			<c:set var="fieldName" value="item-${refundItem.paidServiceBillRefundId}"/>

				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${psb.service.facilityServicePrice.name}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.paidItem.paidQty}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.refQuantity}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${psb.unitPrice}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.refundReason}</td>
					
					<c:if test="${refundItem.approved==false && refundItem.declined==false}">
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
					Approved<input type="radio" value="1_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="approve_${refundItem.paidServiceBillRefundId}" class="approval"/>				
					Declined<input type="radio" value="0_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="decline_${refundItem.paidServiceBillRefundId}" class=decline />
					<textarea name="declineNote_${refundItem.paidServiceBillRefundId}" id="declineNote_${refundItem.paidServiceBillRefundId}" rows="1" cols="20" value="" style="display: none;" ></textarea>
					</c:if>
					
					</td>

					<c:set var="action" value="${refundItem.approved}"/>
					<c:if test="${refundItem.approved }">
					 <td class="rowValue center ${(status.count%2!=0)?'even':''}"><span title='approved' class='closedStutus'><b>V</b></span></td>	
					</c:if>
					
					<c:if test="${not refundItem.approved }">
					 <td class="rowValue center ${(status.count%2!=0)?'even':''}"><span title='declined' class='closedStutus'><b>X</b></span></td>	
					</c:if>

				</tr>
			</c:forEach>
	
		</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>