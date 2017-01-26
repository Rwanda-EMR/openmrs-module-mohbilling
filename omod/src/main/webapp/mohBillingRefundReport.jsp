<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
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
.insurances, .thirdParties,.time,.billCreator,.billStatus,.services,.deposit,.paymentType{
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
	Payment Refunds Report
</h2>

<c:import url="mohBillingReportParameters.jsp" />


<c:if test="${!empty confirmedRefunds}">	
<b>Refund list: </b> : <b style="color: blue;">(${fn:length(confirmedRefunds)})</b>
<div class="box">
<table style="width: 70%">
 <tr>
 <th class="columnHeader">#</th>
 <th class="columnHeader">Refund Id</th>
 <th class="columnHeader">Payment Id</th>
 <th class="columnHeader">Cashier Names</th>
 <th class="columnHeader">Submitted On</th>
 <th class="columnHeader">Approved By</th>
 <th class="columnHeader">Confirmed By</th>
 <th class="columnHeader">Action</th>
 </tr>
 <c:forEach items="${confirmedRefunds}" var="refund" varStatus="status">
 <tr>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
  <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.refundId}</td>
  <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.billPayment.billPaymentId}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.creator.personName}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.createdDate}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.creator.personName}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}">${refund.creator.personName}</td>
 <td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="viewRefundedItems.form?refundId=${refund.refundId}">View Items / Reasons</a></td>
 </tr>
 </c:forEach>
</table>
</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>