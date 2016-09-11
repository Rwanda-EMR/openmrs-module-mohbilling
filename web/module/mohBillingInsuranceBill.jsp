<%@ include file="/WEB-INF/template/include.jsp"%>



<table border="1">
	<tr>
		<th>#</th>
		<th>Date</th>
		<th>Card NUMBER </th>
		<th>AGE</th>
		<th>Gender</th>
		<th>BENEFICIARY'S NAMES</th>
		<th>CONSULTATION </th>
		<th>LABORATORY</th>
		<th>HOSPITALIZATION</th>
		<th>PROCEDURES  AND MATERIALS</th>
		<th>OTHER CONSUMABLES</th>
		<th>DRUGS</th>
		<th>Amount</th>
	</tr>
    
	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<tr>	
		    <td>${status.count}</td>
			<td>${asr.consommation.createdDate}</td>
			<td>${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td>${asr.consommation.beneficiary.patient.age}</td>
			<td>${asr.consommation.beneficiary.patient.gender}</td>
			<td>${asr.consommation.beneficiary.patient.personName}</td>
			<c:forEach items="${asr.revenues }" var="revenue">
			 
			 <td>${revenue.dueAmount }</td>
			 
			</c:forEach>
		<td>${asr.allDueAmounts }</td>
	    </tr>
	</c:forEach>

	
	

</table>