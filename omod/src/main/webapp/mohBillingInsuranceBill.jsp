<%@ include file="/WEB-INF/template/include.jsp"%>



<table>
	<tr>
		<th class="columnHeader">#</th>
		<th class="columnHeader">Date</th>
		<th class="columnHeader">Card NUMBER </th>
		<th class="columnHeader">AGE</th>
		<th class="columnHeader">Gender</th>
		<th class="columnHeader">BENEFICIARY'S NAMES</th>
		<th class="columnHeader">CONSULTATION </th>
		<th class="columnHeader">LABORATORY</th>
		<th class="columnHeader">HOSPITALIZATION</th>
		<th class="columnHeader">PROCEDURES  AND MATERIALS</th>
		<th class="columnHeader">OTHER CONSUMABLES</th>
		<th class="columnHeader">DRUGS</th>
		<th class="columnHeader">Amount</th>
	</tr>
    
	<c:forEach items="${listOfAllServicesRevenue}" var="asr" varStatus="status">
		<tr>	
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.createdDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.age}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.gender}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
			<c:forEach items="${asr.revenues }" var="revenue">
			 
			 <td class="rowValue ${(status.count%2!=0)?'even':''}">${revenue.dueAmount }</td>
			 
			</c:forEach>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b>${asr.allDueAmounts }</b></td>
	    </tr>
	</c:forEach>

	
	

</table>