<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


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
<b class="boxHeader">${param.type} Form</b>
<div class="box">
	<form action="transaction.form?patientId=${param.patientId }&patientAccountId=${param.patientAccountId}&save=true&type=${type}" method="post">
		<table>
			<tr>
				<td>Amount</td>
				<td><input type="text" name="amount"/></td>
			</tr>
			<tr>
				<td>Reason </td>
				<!-- <td><input type="text" name="reason"/></td> -->
				<td>
				<select name="reason">
				<c:choose>
					<c:when test="${param.type == 'Withdrawal' }">
						<option value="withdrawal">Withdrawal</option>
					</c:when>
					<c:otherwise>
						  <option value="deposit">Deposit</option>
					</c:otherwise>
				</c:choose>
				</select>
				</td>
			</tr>
			<tr>
				<td>Collector</td>
				<!--<td><openmrs_tag:userField formFieldName="collector" initialValue="${authUser.userId}"/></td> -->
				<td><input type="text" name="collector" value="${authenticatedUser.username}" disabled="disabled"/></td>
			</tr>
			<tr>
			    <c:set var="today" value="<%=new java.util.Date()%>" />
			    <td> Date:</td>
				<td><input type="text" name="receivedDate"  value="<fmt:formatDate  type="date" pattern="yyyy-MM-dd HH:mm:ss" value="${today}"/>" disabled="disabled" /></td>
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