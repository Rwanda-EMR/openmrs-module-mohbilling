<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage Billing Reports" otherwise="/login.htm" redirect="/mohbilling/cohort.form" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.PrintArea.js" />	
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<script type="text/javascript" language="JavaScript">
	var $bill = jQuery.noConflict();

	$bill(document).ready(function() {
		$bill('.meta').hide();
		
		$bill("input#print_button").click(function() {
			$bill('.meta').show();
			$bill("div.printarea").printArea();
			$bill('.meta').hide();
		});
	});
	
</script>

<h2><spring:message code="@MODULE_ID@.billing.report"/></h2>

<ul id="menu">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.cohort"/></a>
		</li>
		<openmrs:hasPrivilege privilege="Manage Billing Reports">
			<li>
				<a href="hmisReport.form">HMIS Reports</a>
			</li>
		</openmrs:hasPrivilege>
		      <li class="<c:if test='<%= request.getRequestURI().contains("received")%>'> active</c:if>">
				<a href="received.form"><spring:message code="@MODULE_ID@.billing.Received"/></a>
			</li>
</ul>

<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">


<form action="received.form" method="post" name="">
<input type="hidden" name="patientIdnew" value="${patientId}"/>	
<table>
	<tr>
		<td width="10%">When?</td>
		<td>
		<table>
			<tr>
				<td>On <input type="text" size="11" value="${startDate}"
					name="startDate" onclick="showCalendar(this)" /></td>
			</tr>
			
		</table>
		</td>
		<td>Collector :</td>
		<td><openmrs_tag:userField formFieldName="billCollector" initialValue="${billCollector}"/></td>
	</tr>


</table>
<input type="submit" value="Search" />
</form>

</div>
<br/>



<br/>
<c:if test="${fn:length(billPaymentsByDateAndCollector)!=0}">
<b class="boxHeader"> RECEIVED AMOUNT REPORT</b>
<div class="box">
<table width="40%">
	<tr>
		<td><b><h2>No</h2></b</td>
		<td><b><h2>Date</h2></b</td>
		<td><b><h2>Collector</h2></b</td>
		<td><b><h2>Received Amount</h2></b</td>
	</tr>

   <c:forEach items="${billPaymentsByDateAndCollector}" var="payment" varStatus="status">
   <tr>
		<td>${status.count}</td>
			<td>${payment.createdDate}</td>
			<td>${payment.collector}</td>
			<td>${payment.amountPaid}</td>

	</tr>
</c:forEach>
<tr>
		<td></td>
		<td></td>
		<td></h2></td>
		<td><b><h3>${TotalReceivedAmount}</h3></b></td>
	</tr>
</table>
</div>
</c:if>



<%@ include file="/WEB-INF/template/footer.jsp"%>
