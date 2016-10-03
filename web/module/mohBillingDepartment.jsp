<%@ include file="/WEB-INF/template/include.jsp"%>

<b>Total: ${totalAmountByAllDepart }</b>
<table style="width:70%">
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">SERVICE</th>	
		<c:forEach items="${services}" var="s" varStatus="status">
		<th class="columnHeader">${s.service}</th>	
		</c:forEach> 
		<th class="columnHeader">TOTAL</th>
	</tr>

	<c:forEach items="${departmentsRevenues}" var="dsr" varStatus="status">
	<tr class="rowValue ${(status.count%2!=0)?'even':''}">
	 <td>${status.count}.</td>
	 <td>${dsr.department.name}</td>
	  <c:forEach items="${dsr.paidServiceRevenues}" var="sr" varStatus="status">
		 <td>${sr.paidAmount}</td>
	  </c:forEach>
	  <td><b>${dsr.amount }</b></td>
	</tr>
	</c:forEach>
</table>

