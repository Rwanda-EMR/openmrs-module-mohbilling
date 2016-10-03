<%@ include file="/WEB-INF/template/include.jsp"%>



<table>
	<tr>
		<th class="columnHeader">#</th>
		<th class="columnHeader">Date</th>
		<th class="columnHeader">NAMES</th>
		<th class="columnHeader">CARD Nbr</th>
		<th class="columnHeader">ADMISSION DATE</th>
		<th class="columnHeader">DISCHARGE DATE</th>
		<th class="columnHeader">SERVICE </th>
		<th class="columnHeader">HOSP. DAYS</th>
		<th class="columnHeader">HOSP. FEES</th>
		<th class="columnHeader">CONSULT</th>
		<th class="columnHeader">LABO</th>
		<th class="columnHeader">ECHO</th>
		<th class="columnHeader">XRAY</th>
		<th class="columnHeader">PROCEDURES</th>
		<th class="columnHeader">NURSING CARE</th>
		<th class="columnHeader">IMPRIMES</th>
		<th class="columnHeader">PHARMACY</th>
		<th class="columnHeader">OTHER</th> 

	</tr>

	<c:forEach items="${listOfAllPrivServicesRevenue}" var="asr" varStatus="status">
		<tr>	
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.createdDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.patient.personName}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.beneficiary.insurancePolicy.insuranceCardNo}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.globalBill.admission.admissionDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.globalBill.admission.dischargingDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">?</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${asr.consommation.department.name}</td>
			
			<c:forEach items="${asr.revenues }" var="revenue">
			 <td class="rowValue ${(status.count%2!=0)?'even':''}">${revenue.dueAmount }</td>
			</c:forEach>
		<td class="rowValue ${(status.count%2!=0)?'even':''}"><b>${asr.allDueAmounts }</b></td>
	    </tr>
	</c:forEach>
	

</table>