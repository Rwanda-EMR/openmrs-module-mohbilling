<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<div style="; width: 49%">
			<b class="boxHeader">Patient details</b>
			<div class="box">
				<table>
					<tr>
						<td>Names</td>
						<td> : <b>${patient.personName}</b></td>
						<td>Gender</td>
						<td> : <b>${(patient.gender=='F')?'Female':'Male'}</b></td>
						<c:if test="${patientAccount!=null}">
						<td>Balance : </td>
						<td><div class="amount">${patientAccount.balance}</div></td>
						</c:if>
					</tr>
					<tr>
						<td>Birthdate</td>
						<td> : <b><openmrs:formatDate date="${patient.birthdate}" type="medium" /></b></td>
						<td>Age</td>
						<td> : <b>${patient.age}</b> yrs</td>
					</tr>
					
				</table>
			</div>
</div>

<br/>

<div style="width: 49%">
<b class="boxHeader">Patient Account</b>
<div class="box">
	<form action="transaction.form?patientId=${param.patientId }&patientAccountId=${param.patientAccountId}&save=true" method="post">
		<table>
			<tr>
				<td>Amount</td>
				<td><input type="text" name="amount"/></td>
			</tr>
			<tr>
				<td>Reason </td>
				<td>
				<!-- list to be set in Global Properties -->
				 <select name="reason">
				 	<c:forEach items="${transactionReasons}" var="reason">
				 	<option value="${reason}">${reason}</option>
				 	</c:forEach>
				</select> 
				</td>
			</tr>
			<tr>
				<td>Collector</td>
				<td><openmrs_tag:userField formFieldName="collector" initialValue="${authUser.userId}"/></td>
			</tr>
			<tr>
				<td>Date Received</td>
				<td><input type="text" autocomplete="off" name="receivedDate" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate date='${todayDate}' type="string"/>"/></td>
			</tr>
			
			<tr>
				<td></td>
				<td><input type="submit" value="Submit"/></td>
			</tr>
		</table>
	</form>
</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
