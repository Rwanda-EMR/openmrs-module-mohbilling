<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2>Patient Bill Payment</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>
<br/>

<b class="boxHeader">${patientBill.beneficiary.patient.personName}'s Bill as on ${patientBill.createdDate}</b>
<div class="box">
	<form action="patientBillPayment.form?patientBillId=${param.patientBillId}&ipCardNumber=${param.ipCardNumber}&save=true" method="post" id="formSaveBillPayment">
		<table width="99%">
			<tr>
				<th class="columnHeader"></th>
				<th class="columnHeader">Service</td>
				<th class="columnHeader center">Qty</td>
				<th class="columnHeader right">Unit Price (Rwf)</td>
				<th class="columnHeader right">Price (Rwf)</td>
				<th class="columnHeader right">Insurance : ${insurancePolicy.insurance.currentRate.rate} %</td>
				<th class="columnHeader right">Patient : ${100-insurancePolicy.insurance.currentRate.rate} %</td>
			</tr>
			<c:if test="${empty patientBill.billItems}"><tr><td colspan="7"><center>No Patient Bill Item found !</center></td></tr></c:if>
			<c:set var="totalBillInsurance" value="0"/>
			<c:set var="totalBillPatient" value="0"/>
			<c:forEach items="${patientBill.billItems}" var="billItem" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${billItem.service.facilityServicePrice.name}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billItem.quantity}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice*billItem.quantity}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">
						${((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100}
						<c:set var="totalBillInsurance" value="${totalBillInsurance+(((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100)}"/>
					</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">
						${((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100}
						<c:set var="totalBillPatient" value="${totalBillPatient+(((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100)}"/>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="4"></td>
				<td><div style="text-align: right;"><b>Total : </b></div></td>
				<td><div class="amount">${totalBillInsurance}</div></td>
				<td><div class="amount">${totalBillPatient}</div></td>
			</tr>
			<tr>
				<td colspan="7"><hr/></td>
			</tr>
			<tr style="font-size: 1em">
				<td><b>Collector</b></td>
				<td colspan="2"><openmrs_tag:userField formFieldName="billCollector" initialValue="${authUser.userId}"/></td>
				<td colspan="2"></td>
				<td><div style="text-align: right;"><b>Amount Paid</b></div></td>
				<td><div class="amount">${billingtag:amountPaidForPatientBill(patientBill.patientBillId)}</div></td>
			</tr>
			<tr>
				<td><b>Received Date</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="dateBillReceived" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate date='${todayDate}' type="string"/>"/></td>
				<td colspan="2"></td>
				<td><div style="text-align: right;"><b>Rest</b></div></td>
				<td><div class="amount">${billingtag:amountNotPaidForPatientBill(patientBill.patientBillId)}</div></td>
			</tr>
			<tr>
				<td><b>Received Cash</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="receivedCash" size="11" class="numbers" value=""/></td>
				<td colspan="4"></td>
			</tr>
			<tr>
				<td colspan="7"><hr/></td>
			</tr>
			<tr style="font-size: 1.2em">
				<td colspan="2"><input type="button" onclick="savePatientBillPayment();" value="Confirm Bill" style="min-width: 200px;"/></td>
				<td colspan="3"></td>
				<td colspan="2"><div style="text-align: right;"><a href="printPDFPatientBill.form?patientBillId=${patientBill.patientBillId}">Print Bill</a></div></td>
			</tr>
		</table>
	</form>
</div>
<br/>

<b class="boxHeader">Bills History</b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader">Date of Bill</td>
			<th class="columnHeader"></th>
			<th class="columnHeader">Created By</td>
			<th class="columnHeader">Policy ID No.</td>
			<th class="columnHeader right">Paid Amount (Rwf)</td>
			<th class="columnHeader right">Total Amount (Rwf)</td>
			<th class="columnHeader right">Rest Amount (Rwf)</td>
			<th class="columnHeader">Status</td>
			<th class="columnHeader"><!-- Bill ID --></td>
		</tr>
		<c:if test="${empty patientBills}"><tr><td colspan="9"><center>No Patient Bill found !</center></td></tr></c:if>
		<c:set value="0" var="index"/>
		<c:forEach items="${patientBills}" var="pb" varStatus="status">
			<tr>
				<c:choose>
				  <c:when test="${pb.createdDate == currentDate}">
				   	<td class="" <c:if test="${index%2!=0}">style="background-color: white;"</c:if>><c:if test="${pb.createdDate!=currentDate}"><openmrs:formatDate date="${pb.createdDate}" type="medium"/><c:set value="${pb.createdDate}" var="currentDate"/></c:if></td>
				  </c:when>
				  <c:otherwise>
				  	<c:set value="${index+1}" var="index"/>
				   	<td class="rowValue"><c:if test="${pb.createdDate!=currentDate}"><openmrs:formatDate date="${pb.createdDate}" type="medium"/><c:set value="${pb.createdDate}" var="currentDate"/></c:if></td>
				  </c:otherwise>
				</c:choose>
				<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}">${pb.createdDate}</td> -->
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${pb.creator.personName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${pb.beneficiary.policyIdNumber}</td>
				<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidForPatientBill(pb.patientBillId)}</td>
				<td class="rowValue right ${(status.count%2!=0)?'even':''}">${pb.amount}</td>
				<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billingtag:amountNotPaidForPatientBill(pb.patientBillId)}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${((billingtag:amountNotPaidForPatientBill(pb.patientBillId))<=0.0)?'PAID':'NOT PAID'}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="patientBillPayment.form?patientBillId=${pb.patientBillId}&ipCardNumber=${pb.beneficiary.policyIdNumber}">View<!-- ${pb.patientBillId} --></a></td>
			</tr>
		</c:forEach>
	</table>
</div>

<script>
	function savePatientBillPayment(){
		if(confirm("Are you sure you want to save the payment ?")){
			document.getElementById("formSaveBillPayment").submit();
		}
	}

	//function printPatientBill(patientBillId){
		
	//}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>