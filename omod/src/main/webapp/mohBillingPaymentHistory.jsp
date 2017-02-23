<%@ include file="/WEB-INF/template/include.jsp"%>
<b class="boxHeader">Bill Payments  History</b>
<div class="box">
	<table width="99%">
		<tr>		 
			<th class="columnHeader"></th>
			<th class="columnHeader">PaymentId</th>		
			<th class="columnHeader">collector</th>			
			<th class="columnHeader right">Paid Amount (Rwf)</th>
			<th class="columnHeader">Paid items</th>
			<th class="columnHeader"></th>		
		</tr>	
		<c:if test="${empty payments}"><tr><td colspan="9"><center>No payments found !</center></td></tr></c:if>
		<c:forEach items="${payments}" var="payment" varStatus="status">
			<c:if test="${not payment.voided}">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${payment.billPaymentId}</td>				
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${payment.collector.personName}</td>
				<td class="rowValue right ${(status.count%2!=0)?'even':''}">${payment.amountPaid}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="searchBillPayment.form?paymentId=${payment.billPaymentId}&consommationId=${consommation.consommationId}">View</a></td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="searchBillPayment.form?paymentId=${payment.billPaymentId}&consommationId=${consommation.consommationId}&print=true">Print</a></td>
			</tr>
			</c:if>
		</c:forEach>
	</table>
</div>