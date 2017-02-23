<%@ include file="/WEB-INF/template/include.jsp"%>
<h2>Edit Consommation</h2>

<div id="consommation">
<div>
<table>
<tr>
<td>Consommation # : <b>${consommation.consommationId}(${consommation.department.name})</b></td>
<td>Global Bill # : <b>${consommation.globalBill.billIdentifier}</b></td>
<c:if test="${empty consommation.patientBill.payments && !consommation.globalBill.closed}">
<td><a href="billing.form?consommationId=${consommation.consommationId}&departmentId=${consommation.department.departmentId}&insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${consommation.globalBill.globalBillId}&addNew=true">Add Item</a></td>
</c:if>
</tr>
</table>
</div>

<div class="box">
	<form action="billing.form?consommationId=${consommation.consommationId}&insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${consommation.globalBill.globalBillId}&departmentId=${param.departmentId}&save=true" method="post" id="formSaveBillPayment">
		<table width="70%" id="tableone">
			<tr>
				<th style="width: 3%" class="columnHeader">#</th>
				<th style="width: 3%" class="columnHeader"><img src="/openmrs/images/trash.gif" title="Remove Item"/></th>
				<th style="width: 30%" class="columnHeader">Service</th>
				<th style="width: 10%" class="columnHeader center">Qty</th>
				<th style="width: 20%" class="columnHeader right">Unit Price (Rwf)</th>
				<th></th>
			</tr>
			<c:if test="${empty consommation.billItems}"><tr><td colspan="7"><center>No consommation found !</center></td></tr></c:if>
			<c:set var="totalBillInsurance" value="0"/>
			<c:set var="totalBillPatient" value="0"/>
			<c:forEach items="${consommation.billItems}" var="billItem" varStatus="status">
			<c:set var="service" value="${billItem.service.facilityServicePrice}"/>
			<c:set var="fieldName" value="item-${consommation.consommationId}-${billItem.patientServiceBillId}"/>
			 <c:if test="${not billItem.voided}">
				<tr>
				    <td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><input type = "checkbox" name = "removeItem_${billItem.patientServiceBillId}" value = "${billItem.patientServiceBillId }"  style="background-color: red;" ></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${service.name}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><input type="text" size="5" name="newQuantity_${billItem.patientServiceBillId}" value="${billItem.quantity}"/></td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice}</td>				
					<td><input type = "checkbox" name = "billItem" value = "${billItem.patientServiceBillId }"   ></td>
				</tr>
			</c:if>
			</c:forEach>		   

			<tr>
				<td colspan="7"><hr/></td>
			</tr>

			
			<tr style="font-size: 1.2em">
				<openmrs:hasPrivilege privilege="Edit Bill">
					<td colspan="2"><input type="submit"  value="Save Consommation" style="min-width: 200px;" onclick="check()"/></td>
				</openmrs:hasPrivilege>
			 <td colspan="3"></td>    
			
			</tr>
		
		</table>
	</form>
</div>
</div>
