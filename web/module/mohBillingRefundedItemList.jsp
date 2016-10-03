<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<script type="text/javascript">
$(function(){
    $('.decline').click(function() {
    	 var itemIdToSplit = $(this).val();
    	 var itemId = itemIdToSplit.split("_");
         if($(this).is(':checked'))
        	$("#declineNote_"+itemId[1]).show();
    });
    $('.approval').click(function() {
   	 var itemIdToSplit = $(this).val();
	 var itemId = itemIdToSplit.split("_");
        if($(this).is(':checked'))
       	 $("#declineNote_"+itemId[1]).hide();
   });
});
</script>

<c:set var="insurancePolicy" value="${consommation.beneficiary.insurancePolicy}"/>
<c:set var="globalBill" value="${consommation.globalBill}"/>
    
<h2>Refund Items submitted for approval </h2>

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
<tr><td>Patient:</td><td>test</td></tr>
<tr><td>Insurance Policy:</td><td>test</td></tr>
</table>
</div>
</div>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div style="text-align: right;">

<br/>
<div class="box">
	<form action="paymentRefund.form?refundId=${refund.refundId}&paymentId=${refund.billPayment.billPaymentId }&edit=true" method="post">
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
			<c:if test="${empty refund.refundedItems}"><tr><td colspan="7"><center>No refund items found !</center></td></tr></c:if>

			<c:forEach items="${refund.refundedItems}" var="refundItem" varStatus="status">
			<c:set var="psb" value="${refundItem.paidItem.billItem}"/>
			<c:set var="fieldName" value="item-${refundItem.paidServiceBillRefundId}"/>
			<c:if test="${not refundItem.paidItem.billItem.voided}">	
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${psb.service.facilityServicePrice.name}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.paidItem.paidQty}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.refQuantity}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${psb.unitPrice}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${refundItem.refundReason}</td>
					
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
					Approved<input type="radio" value="1_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="approve_${refundItem.paidServiceBillRefundId}" class="approval"/>				
					Declined<input type="radio" value="0_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="decline_${refundItem.paidServiceBillRefundId}" class=decline />
					<textarea name="declineNote_${refundItem.paidServiceBillRefundId}" id="declineNote_${refundItem.paidServiceBillRefundId}" rows="1" cols="20" value="" style="display: none;" ></textarea>
					</td>
					
				</tr>
				</c:if>
			</c:forEach>		   
			
			<tr>
				<td colspan="7"><hr/></td>
			</tr>			
			<tr style="font-size: 1.2em">
				
					<td colspan="2"><input type="submit"  value="Confirm" style="min-width: 200px;" class="submitBtn"/></td>
				
			 <td colspan="3"></td>
			</tr>			
		</table>
	</form>
	
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>