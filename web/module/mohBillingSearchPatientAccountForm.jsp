<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>


<b class="boxHeader">Find Patient Account </b>
<div class="box">
<form action="searchPatientAccount.form" method="post">
		<table>
			<tr>

				<td>Patient:</td>
				<td><openmrs:fieldGen type="org.openmrs.Patient" formFieldName="patientId" val="${patient}" /></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Search"/></td>
				<td></td>
			</tr>
		</table>
</form>
</div>
<c:if test="${patientAccount!=null}">
<%-- <b class="boxHeader">Patient Account Summary</b>
<div class="box">
	<table>
		<tr>
<th>Patient Names</th>
<th>Creator</th>
<th>Create Date</th>
<th>Balance</th>
<th>Action</th>
</tr>
<tr>
<td>${patientAccount.patient.personName }</td>
<td>${patientAccount.creator.personName}</td>
<td>${patientAccount.createdDate}</td>
<td>${patientAccount.balance}</td>
<td><a href="transaction.form?patientId=${patientAccount.patient.patientId}&patientAccountId=${patientAccount.patientAccountId }">Deposit</a>|<a>Withdrawal</a>|<a id="view" href="searchPatientAccount.form?patientAccountId=${patientAccount.patientAccountId }">View</a></td>
</tr>
</table>	
</div> --%>


<br/>
<c:if test="${transactions!=null}">
<table>
<tr>
<td>Balance :<b>${patientAccount.balance}</b></td>
<td><a href="transaction.form?patientId=${patientAccount.patient.patientId}&patientAccountId=${patientAccount.patientAccountId }">Deposit</a></td>
<td><a href="transaction.form?patientId=${patientAccount.patient.patientId}&patientAccountId=${patientAccount.patientAccountId }&withdrawal=true">Withdrawal</a></td>
</tr>
</table>
<b class="boxHeader">Transactions Summary </b>
<div class="box">
	<table style="width:100%">
		<tr>
<th style="width:5%">#</th>
<th style="width:8%">Transaction Date</th>
<th style="width:8%">Reason</th>
<th style="width:12%">Collector</th>
<th style="width:12%">Creator</th>
<th style="width:6%">Cash In</th>
<th style="width:6%">Cash Out</th>
<th style="width:6%">Balance</th>
</tr>

    <c:forEach items="${transactions}" var="trans" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatDate pattern="yyyy-MM-dd" value="${trans.transactionDate}" /></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.reason}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.collector.personName}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.creator.personName}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.amount}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.amount}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${trans.amount}</td>
				</tr>
	</c:forEach>
			

</table>	
</div>
</c:if>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
