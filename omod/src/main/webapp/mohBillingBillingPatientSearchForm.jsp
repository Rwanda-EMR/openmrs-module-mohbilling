<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="Check Patient Bill Payment" otherwise="/login.htm" redirect="/module/@MODULE_ID@/checkPatientBillPayment.form" />

<%@ include file="templates/mohBillingBillHeader.jsp"%>
<script type="text/javascript">
			var $j = jQuery.noConflict();
</script>
   <script>

 $j(document).ready(function() {
     // Your code goes here
     // This code will run when the document is ready

        // Select the input field by its id

        var today = new Date();

        // Format the date in YYYY-MM-DD format (required for input type="date")
        var formattedDate = today.toISOString().substring(0, 10).split("-");
        var formatDate = formattedDate[2]+"/"+formattedDate[1]+"/"+formattedDate[0];
        // Set the value of the input field to today's date

       // alert(formatDate);
        $j("#startDateid").value=formatDate;
        $j("#endDateid").value=formatDate;
        //document.getElementById(this).value = formatDate;
       //document.getElementById("dateInput2").value = formatDate;
 });
    </script>


<h2><spring:message code="@MODULE_ID@.billing" /></h2>


<b class="boxHeader">Open Bill Confirmation page</b>
<div class="box">
<form action="openConfirmationPage.form" method="get">
		<table>
			<tr>
				<td>Start Date</td>
				<td><input type="text" name="startDate" id="startDateid" onclick="showCalendar(this)" /></td>
			</tr>
			<tr>
            	<td>End Date</td>
            	<td><input type="text" name="endDate" id="endDateid" onclick="showCalendar(this)" /></td>
            </tr>
			<tr>
				<td colspan="2"><center><input type="submit" value="Open"/></center></td>
			</tr>
		</table>
</form>
</div>


<div id="search_policy">
	<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="globalBill.list" />
</div>

<br/> &nbsp; or<br/><br/>
 
<b class="boxHeader">Search by BIll Identifier</b>
<div class="box">
<form action="globalBill.list?billIdentifier=1984" method="get">
		<table>
			<tr>
				<td>Bill Identifier Id</td>
				<td><input type="text" name="billIdentifier"/></td>
				<td></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Search"/></td>
				<td></td>
			</tr>
		</table>
</form>
</div>


