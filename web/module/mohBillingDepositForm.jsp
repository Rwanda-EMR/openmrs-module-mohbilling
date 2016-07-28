<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!--  
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
-->

<!-- script to create a pop up windows for unpaid bills -->
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_style.css" /> 


<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingDepositHeader.jsp"%>
<script type="text/javascript">
	$(document).ready(function() {
		$("#deposits a").click(function() { 
			  $("#deposits").hide(); 
			});

	});
</script>

<h2>Manage Deposit</h2>
<form action="deposit.form?save=true" method="post">
<b class="boxHeader">Section I >> Owner</b>
<div class="box">
	<table>
		<tr>
		 <td>Patient:   </td>
		 <td width="300px;">${patient.personName }</td>
		</tr>
		</table>
	</div>
	<br /> 
<input type="hidden" name="patientId" value="${patient.patientId}" />

<b class="boxHeader">Section II >> Deposit </b>
<div class="box">
		<table>
			<tr>
				<td>Amount</td>
				<td><input type="text" name="depositAmount" value="${deposit.amount }"/></td>
			</tr>
			<tr>
				<td>Date</td>
				<td><input type="text" name="depositDate" value="${deposit.depositDate }"/></td><td>(dd/mm/yyyy)</td>
			</tr>
			<tr>
				<td>Reason</td>
				<td>
				<!-- list to be set in Global Properties -->
				 <select name="depositReason">
				 	<c:forEach items="${depositReasons}" var="depositReason">
				 	<option value="${depositReason}" <c:if test='${deposit.depositReason==depositReason}'>selected='selected'</c:if>>${depositReason}</option>
				 	</c:forEach>
				</select> 
				</td>
			</tr>
			<tr>
			<td>Collector:</td>
			<td colspan="2"><openmrs_tag:userField formFieldName="depositCollector" initialValue="${authUser.userId}"/></td>
			</tr>
		</table>
		<br /> <input type="submit" value="Save Deposit" id="submitButtonId" />
</div>
</form>
<br/><br/>
<div id="deposits">
<form action="depositList.list" method="post">
<c:if test="${fn:length(depositsList)!=0}">
<b class="boxHeader">Total Deposit : ${totalDepositAmount } <strong>FRW</strong></b>
<div class="box">
<table>
	<tr>
		<th class="columnHeader">#.</th>
		<th class="columnHeader">Patient First Name</th>
		<th class="columnHeader">Patient Last Name</th>
		<th class="columnHeader">Amount</th>
		<th class="columnHeader">Deposit Reason</th>
		<th class="columnHeader">Collector</th>
		<th class="columnHeader">Date</th>
		<th class="columnHeader">Action</th>
	</tr>
	<c:forEach items="${depositsList}" var="deposit" varStatus="status">	
	
		<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.patient.givenName}</td>	
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.patient.familyName}</td>			
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.amount}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.depositReason }</td>	
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.cashier }</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${deposit.depositDate }</td>	
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><a id="edit" href="deposit.form?depositId=${deposit.depositId}&patientId=${patient.patientId}">Edit</a></td>
		</tr>
		
	</c:forEach>
</table>
</div>
</c:if>
</form>
</div>
<br/>

<div id="retire">
<c:if test="${deposit.depositId ne null}">
	<b class="boxHeader">Retire this Facility Service</b>
	<div class="box">
		<form action="deposit.form?retire=true" method="post">
			<table>
				<tr>
					<td>Retire Reason</td>
					<td><input type="text" name="depositRetireReason" size="60"/></td>
				</tr>
				<tr>
					<td><input type="hidden" name="depositId" value="${deposit.depositId}"/></td>
					<td><input type="submit" value="Retire Deposit"/></td>
				</tr>
			</table>
		</form>
	</div>
</c:if>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>
