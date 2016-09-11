<%@ include file="/WEB-INF/template/include.jsp"%>



<table style="width:70%">
	<tr>
		<th style="width:3%">#.</th>
		<th style="width:10%">Service Name</th>	
		<th style="width:3%">Due Amount</th>
	</tr>
	
	<c:forEach items="${allServicesRevenue.revenues}" var="revenue" varStatus="status">
		 <tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${revenue.service}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${revenue.dueAmount}</td>							
		 </tr>		
	</c:forEach>
	<tr>
	<td></td>
	<td><div style="text-align: right;"><b>Total : </b></div></td>
	<td >${allServicesRevenue.allDueAmounts}</td>
	
	</tr>
</table>


