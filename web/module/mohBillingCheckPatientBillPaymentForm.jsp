<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2>Checking Patient Bill Payment</h2>

<b class="boxHeader">Patient & Period</b>
<div class="box">
	<form action="checkPatientBillPayment.form" method="get">
		<table>
			<tr>
				<td>Patient</td>
				<td><openmrs_tag:patientField formFieldName="patientId" initialValue="${param.patientId}"/></td>
				<td></td>
			</tr>
			<tr>
				<td>Period</td>
				<td>From:<input autocomplete="off" type="text" name="dateFrom" size="11" value="${param.dateFrom}" onclick="showCalendar(this);"/></td>
				<td>To:<input autocomplete="off" type="text" name="dateTo" size="11" value="${param.dateTo}" onclick="showCalendar(this);"/></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Check Payment"/></td>
				<td></td>
			</tr>
		</table>
	</form>
</div>
<br/>

<b class="boxHeader">Bill Payment</b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Service</td>
			<th class="columnHeader center">Qty</td>
			<th class="columnHeader right">Unit Price (Rwf)</td>
			<th class="columnHeader right">Price (Rwf)</td>
			<th class="columnHeader right">Status</td>
			<th class="columnHeader right"></th>
		</tr>
		<c:forEach items="${patientBills}" var="patientBill">
			<c:if test="${empty patientBill.billItems}"><tr><td colspan="6"><center>No Patient Bill Item found !</center></td></tr></c:if>
			<c:if test="${!empty patientBill.billItems}">
				<tr>
					<td class="columnHeader"><a href="patientBillPayment.form?patientBillId=${patientBill.patientBillId}&ipCardNumber=${patientBill.beneficiary.policyIdNumber}">View</a></td>
					<th class="columnHeader" style="font-size: 1em; font-weight: bold;" colspan="5"><center>Date: ${patientBill.createdDate}, Total Amount : ${patientBill.amount} Rwf, Insurance : ${patientBill.beneficiary.insurancePolicy.insurance.name}, Policy Card No. : ${patientBill.beneficiary.policyIdNumber}</center></th>
					<td class="columnHeader">
						<openmrs:hasPrivilege privilege="Manage Refund Bill">
							<a href="refundBill.form?patientBillId=${patientBill.patientBillId}&ipCardNumber=${patientBill.beneficiary.policyIdNumber}">Refund</a>
						</openmrs:hasPrivilege>
					</td>
				</tr>
			</c:if>
			<c:forEach items="${patientBill.billItems}" var="billItem" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${billItem.service.facilityServicePrice.name}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billItem.quantity}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice*billItem.quantity}</td>
					<td colspan="2" class="rowValue right ${(status.count%2!=0)?'even':''}">${((billingtag:amountNotPaidForPatientBill(patientBill.patientBillId))<=0.0)?'PAID':'NOT PAID'}</td>
				</tr>
			</c:forEach>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>