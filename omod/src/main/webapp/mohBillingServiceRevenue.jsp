<%@ include file="/WEB-INF/template/include.jsp"%>

<table style="width:70%">
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">Service Name</th>	
		<th class="columnHeader">Due Amount</th>
	</tr>

	<c:forEach items="${paidServiceRevenues}" var="psr" varStatus="status">
	<tr>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}"> ${psr.service }</td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}">${psr.paidAmount}</td>
	</tr>
	</c:forEach>
</table>


