<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<script language="javascript" type="text/javascript">

var $k= jQuery.noConflict();

</script>


<ul id="menu">
	<li
		class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
	<a href="cohort.form"><spring:message
		code="@MODULE_ID@.billing.cohort" /></a></li>

	<li
		class="<c:if test='<%= request.getRequestURI().contains("Recovery")%>'> active</c:if>">
	<a href="ReportRecovery.form"><spring:message
		code="@MODULE_ID@.billing.recovery" /></a></li>
</ul>
<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">


<form action="ReportRecovery.form" method="post" name="">

<table>
	<tr>
		<td width="10%">When?</td>
		<td>
		<table>
			<tr>
				<td>On Or After <input type="text" size="11" value="${startDate}"
					name="startDate" onclick="showCalendar(this)" /></td>
			</tr>
			<tr>
				<td>On Or Before <input type="text" size="11" value="${endDate}"
					name="endDate" onclick="showCalendar(this)" /></td>
			</tr>
		</table>
		</td>
	</tr>

	<tr>
		<td>Insurance:</td>
		<td><select name="insurance" >
			<option selected="selected" value="${insuranceIdInt}">  
			
			<c:choose>
             <c:when test="${insuranceIdInt != nul}"> ${insurance.name} 

             </c:when>

               <c:otherwise>--select-- 
             </c:otherwise>
            </c:choose>
		
			 
      </option>
			<c:forEach items="${allInsurances}" var="ins">
				<option value="${ins.insuranceId}">${ins.name}</option>
			</c:forEach>
		</select></td>
	</tr>
</table>

<input type="submit" value="Search" /> <br>
<br>	
	<c:if test="${!empty recoveryReports}">
	
     <b class="boxHeader"> <input name="print"
	type="submit" value="PDF Print" style="display: ri;" /></b>	
	</c:if>
	
</form>

</div>
<c:if test="${!empty oneGuvenInsuranceDue}">
	<div class="box">

	<table width="100%">
		<tr>
			<td><h3><b>Insurance Name</b> </h3></td>
			<td><h3><b>Month</b> </h3></td>
			<td><h3><b>Start period</b> </h3></td>
			<td><h3><b>End Period</b> </h3></td>
			<td><h3><b>Amount To Pay</b> </h3></td>
			<td><h3><b>Paid amount</b> </h3></td>
			<td><h3><b>Remaining Amount</b> </h3></td>
		</tr>
		<tr>
			<td>${insuranceName}</td>
			<td>${month}</td>
			<td>${startDate}</td>
			<td>${endDate}</td>
			<td>${oneGuvenInsuranceDue}</td>
			<td>${paidAmount}</td>
			<td>${oneGuvenInsuranceDue - paidAmount} </td>
		</tr>
	</table>

	</div>
</c:if>
<c:if test="${!empty recoveryReports}">
	<div class="box">

	<table width="100%">
		<tr >
			<td><h3><b>Number</b> </h3></td>
			<td><h3><b>Insurance Name</b> </h3></td>
			<td><h3><b>Month</b> </h3></td>
			<td><h3><b>Start period</b> </h3></td>
			<td><h3><b>End Period</b> </h3></td>
			<td><h3><b>Amount To Pay</b> </h3></td>
			<td><h3><b>Paid Amount</b> </h3></td>
			<td><h3><b>Remaining Amount</b> </h3></td>
			<td><h3><b>Payment Date</b> </h3></td>
		</tr>
		<c:forEach items="${recoveryReports}"
			var="recoveryReport" varStatus="status">
			<tr>
				<td>${status.count}</td>
				<td>${recoveryReport.insuranceName}</td>
					<td>${month}</td>
				<td>${recoveryReport.startDateStr}</td>
			     <td>${recoveryReport.endDateStr}</td>
				<td>${recoveryReport.insuranceDueAmount} ${SameAsBefore}</td>
				<td>${recoveryReport.paidAmount}</td>
				<td>${recoveryReport.remainingAmount}</td>
				<td>${recoveryReport.paidDate}</td>
				
			</tr>

		</c:forEach>
		<tr>
				<td> <h3><b> Totals</b> </h3> </td>
				<td><h3><b> </b></h3></td>
				<td><h3><b> </b></h3></td>
				<td><h3><b> </b></h3></td>
				<td><h3><b> </b></h3></td>
			 
				<td><h3><b> ${totalAmountTobePaid} </b></h3></td>
				<td><h3><b> ${totalPaidAmount}</b></h3></td>
				<td><h3><b> ${totalRemainingAmount}</b></h3></td>
				
			</tr>
	</table>
	
	</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>
	