<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="insurancePolicy" value="${consommation.beneficiary.insurancePolicy}"/>
<c:set var="globalBill" value="${consommation.globalBill}"/>

<div style="text-align: right;"><a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add consommation</a></div>
<h2>Edit Consommation</h2>
<br/>
<div class="box">
	<form action="consommation.form?consommationId=${consommation.consommationId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&edit=true" method="post" id="formSaveBillPayment">
		<table width="99%" id="tableone">
			<tr>
				<th class="columnHeader"></th>
				<th class="columnHeader">Service</td>
				<th class="columnHeader center">Qty</td>
				<th class="columnHeader right">Unit Price (Rwf)</td>
				<th class="columnHeader right">Price (Rwf)</td>
				<th class="columnHeader right">Insurance : ${insurancePolicy.insurance.currentRate.rate} %</td>
				<th class="columnHeader right">Patient : ${100-insurancePolicy.insurance.currentRate.rate} %</td>
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
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${service.name}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><input type="text" size="5" name="quantity-${billItem.patientServiceBillId}" value="${billItem.quantity}"/></td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${billItem.unitPrice*billItem.quantity}" type="number" pattern="#.##"/></td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">					  
						<fmt:formatNumber value="${((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100}" type="number" pattern="#.##"/>
						<c:set var="totalBillInsurance" value="${totalBillInsurance+(((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100)}"/>
					</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">							
						 <fmt:formatNumber value="${((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100}" type="number" pattern="#.##"/>
						 <c:set var="totalBillPatient" value="${totalBillPatient+(((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100)}"/>
					</td>							
				
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


<script>
	function savePatientBillPayment(){
		if(confirm("Are you sure you want to save the payment ?")){
			document.getElementById("formSaveBillPayment").submit();
		}
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>