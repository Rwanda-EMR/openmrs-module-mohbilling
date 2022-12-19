<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>

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
.insurances, .thirdParties,.time,.timelabel,.billCreator,.billStatus,.services,.deposit,.paymentType,.reportType{
     display: none;
}
a.print {
    border: 2px solid #009900;
    background-color: #aabbcc;
    color: #ffffff;
    text-decoration: none;
}

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

<h2>
	Payment Refunds Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />


<c:if test="${!empty confirmedRefunds}">	
<b>Refund list: </b> : <b style="color: blue;">(${fn:length(confirmedRefunds)})</b>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<b>PDF</b>
<div class="box">
<table width="100%">
 <tr>
 <th class="columnHeader">#</th>
 <th class="columnHeader">Refund Id</th>
 <th class="columnHeader">Payment Id</th>
 <th class="columnHeader">Cashier Names</th>
 <th class="columnHeader">Submitted On</th>
 <th class="columnHeader">Approved By</th>
 <th class="columnHeader">Confirmed By</th>
 <th class="columnHeader">Refunded Items Details</th>
 </tr>
 <c:forEach items="${confirmedRefunds}" var="refund" varStatus="status">
 <tr>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
  <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.refundId}</td>
  <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.billPayment.billPaymentId}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.creator.personName}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.createdDate}</td>
      <c:forEach items="${refund.getRefundedItems()}" var="psr" varStatus="stat">
          <c:if test="${stat.count==1}">
              <td class="rowValue ${(status.count%2!=0)?'even':''}">${psr.approvedBy.personName}</td>
          </c:if>
      </c:forEach>
 <!-- <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.creator.personName}</td> -->
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.refundedBy.personName}</td>
<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="viewRefundedItems.form?refundId=${refund.refundId}">View Items / Reasons</a></td> -->
<td class="rowValue ${(status.count%2!=0)?'even':''}">
<table>
			<tr>
				<td></td>
				<td width="25%"><b>Service Name</b></td>
				<td colspan="2"><b>Qty Paid</b></td>
				<td colspan="6"><b>Refund Qty</b></td>
				<td colspan="3"><b>Unit Price (Rwf)</b></td>
				<td><b>Reason</b></td>
				<td><b>Action </b></td>
				<td></td>
			</tr>


			<c:forEach items="${refund.refundedItems}" var="refundItem" varStatus="status">
			<c:set var="psb" value="${refundItem.paidItem.billItem}"/>
			<c:set var="fieldName" value="item-${refundItem.paidServiceBillRefundId}"/>

				<tr>
					<td>${status.count}.</td>
					<td width="25%" >${psb.service.facilityServicePrice.name}</td>
					<td colspan="6">${refundItem.paidItem.paidQty}</td>
					<td colspan="4">${refundItem.refQuantity}</td>
					<td colspan="1">${psb.unitPrice}</td>
					<td>${refundItem.refundReason}</td>

					<c:if test="${refundItem.approved==false && refundItem.declined==false}">
					<td>
					Approved<input type="radio" value="1_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="approve_${refundItem.paidServiceBillRefundId}" class="approval"/>
					Declined<input type="radio" value="0_${refundItem.paidServiceBillRefundId}" name="approveRadio_${refundItem.paidServiceBillRefundId}" id="decline_${refundItem.paidServiceBillRefundId}" class=decline />
					<textarea name="declineNote_${refundItem.paidServiceBillRefundId}" id="declineNote_${refundItem.paidServiceBillRefundId}" rows="1" cols="20" value="" style="display: none;" ></textarea>
					</c:if>

					</td>


					<c:set var="action" value="${refundItem.approved}"/>
					<c:if test="${refundItem.approved }">
					 <td><span title='approved' class='closedStutus'><b>V</b></span></td>
					</c:if>

					<c:if test="${not refundItem.approved }">
					 <td><span title='declined' class='closedStutus'><b>X</b></span></td>
					</c:if>

				</tr>
			</c:forEach>

		</table>
</td>
 </tr>
 </c:forEach>
</table>
</div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>