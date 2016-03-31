<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage Billing Reports" otherwise="/login.htm" redirect="/mohbilling/cohort.form" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.PrintArea.js" />	
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!--  
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
-->

<!-- script to create a pop up windows for unpaid bills -->
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_style.css" /> 
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_script.js" />  

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<!-- 
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
 -->
<script type="text/javascript" language="JavaScript">
var $b = jQuery.noConflict();
$b(document).ready(function(){
    $b('#select_all').on('click',function(){
        if(this.checked){
            $b('.checkbox').each(function(){
                this.checked = true;
            });
        }else{
             $b('.checkbox').each(function(){
                this.checked = false;
            });
        }
    });
    
    $b('.checkbox').on('click',function(){
        if($b('.checkbox:checked').length == $b('.checkbox').length){
            $b('#select_all').prop('checked',true);
        }else{
            $b('#select_all').prop('checked',false);
        }
    });
});
</script>



<h2><spring:message code="@MODULE_ID@.billing.report"/></h2>

<ul id="menu">
        <openmrs:hasPrivilege privilege="Billing Reports - View Find Bills">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.cohort"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Billing Reports - View Payments">
	    <li>
			<a href="received.form"><spring:message code="@MODULE_ID@.billing.received"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Revenue">
		 <li>
			<a href="recettes.form"><spring:message code="@MODULE_ID@.billing.revenue"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Invoice">
		<li>
			<a href="invoice.form"><spring:message code="@MODULE_ID@.billing.invoice"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Releve">
		<li>
			<a href="facture.form"><spring:message code="@MODULE_ID@.billing.facture"/></a>
		</li>
		</openmrs:hasPrivilege>
		<li>
			<a href="refundBillReport.form">Refunding report</a>
		</li>
		
		<!-- 
		<li>
			<a href="hmisReport.form">HMIS Reports</a>
		</li>
		 -->
</ul>

<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">


<form action="cohort.form" method="post" name="">
<input type="hidden" name="patientIdnew" value="${patientId}"/>	
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
		<td>Bill Creator :</td>
		<td><openmrs_tag:userField formFieldName="billCreator" initialValue="${billCreator}"/></td>
		
		<td></td><td></td><td></td><td></td><td><td></td><td></td>
		<!-- <td><a class="topopup" id="alert" style="color: red" href="javascript:toggleDiv('myContent');" style="background-color: #ccc; padding: 5px 10px;"><strong>Alerts(${alertSize})</strong></a></td> -->

	</tr>

	<tr>
		<td>Insurance:</td>
		<td><select name="insurance">
				<option selected="selected" value="${insurance.insuranceId}">
					<c:choose>
						<c:when test="${insurance!=null}">${insurance.name}</c:when>
						<c:otherwise>--Select insurance--</c:otherwise>
					</c:choose>
				</option>
				<c:forEach items="${allInsurances}" var="ins">
					<option value="${ins.insuranceId}">${ins.name}</option>
				</c:forEach>
			</select>
		</td>
		<td>Bill Status</td>
		<td>
			<select name="billStatus">
				<option value="">---</option>
				<option value="FULLY PAID" ${billStatus== 'FULLY PAID' ? 'selected' : ''}>FULLY PAID</option>
				<option value="UNPAID" ${billStatus== 'UNPAID' ? 'selected' : ''}>UNPAID</option>
				<option value="PARTLY PAID" ${billStatus== 'PARTLY PAID' ? 'selected' : ''}>PARTLY PAID</option>
			</select>
		</td>

	</tr>

	<tr>
		<td>Patient</td>
		<td>
			<openmrs_tag:patientField formFieldName="patientId" initialValue="${patientId}" />
		</td>
		<!--td>Facility Services</td>
		<td>
			<select name="serviceId">
				<option selected="selected" value="${serviceId}">
					<c:choose>
						<c:when test="${serviceId!=null}">${serviceId}</c:when>             
				    	<c:otherwise>--Select service--</c:otherwise>
				    </c:choose>
				</option>
				<c:forEach items="${categories}" var="service">
					<option value="${service}">${service}</option>
				</c:forEach>
			</select>
		</td-->
		
	</tr>

</table>
<input type="submit" value="Search" />
</form>

</div>
<br/>

<br/>
<c:if test="${fn:length(billObj)!=0}">
<div class="box">
<b class="boxHeader">Search results </b>

<form action="cohort.form?print=true" method="post" style="display: inline;">
	<!-- <input type="submit" class="list_exportBt" value="PDF" title="Export to PDF"/>
	 -->

<br />
<br />
<table width="99%">
	<tr>
		<td>No</td>
		<td>Date</td>
		<td>Policy Id Number</td>
		<td>Beneficiary</td>

		<td>Billable Services</td>

		<td>Insurance Name</td>
		<td>Insurance due</td>
		<td>Patient Due</td>
		<td>Received Amount</td>
		<td>Amount</td>
		<td>Status</td>
		<td></td>
		
	</tr>

	<c:forEach items="${billObj}" var="obj" varStatus="status">
		<tr>
			<td class="rowValue">${status.count}</td>
			<td class="rowValue">${obj[0]}</td>
			<td class="rowValue"><b>${obj[1]}</b></td>
			<td class="rowValue">${obj[2]}</td>
			<td class="rowValue">

			<table>

				<tr>
				    <td></td>
					<td><b>Service Name</b></td>
					<td colspan="2"><b>Price</b></td>
					<td colspan="6"><b>Quantity</b></td>
					<td colspan="3"><b>Total</b></td>
				</tr>
				<c:forEach items="${obj[3]}" var="srvc" varStatus="status">

					<tr>
					    <td>${status.count}-</td>
						<td>
						${srvc.service.facilityServicePrice.name}
						</td>

						<td colspan="4">${srvc.unitPrice }</td>
						<td colspan="6">${srvc.quantity}</td>
						<td colspan="1">${srvc.unitPrice*srvc.quantity}</td>

					</tr>
				</c:forEach>
			</table>
			</td>

			<td class="rowValue">${obj[4]}</td>
			<td class="rowAmountValue">${obj[5]}</td>
			<td class="rowAmountValue">${obj[6]}</td>
			<td class="rowAmountValue">${obj[7]}</td>
			<td class="rowAmountValue"><b style="color: blue;">${obj[8]}</b></td>
			<td class="rowAmountValue" style="color: green; font-weight: bold;">${obj[9]}</td>
			<td class="rowTotalValue"><a href="patientBillPayment.form?patientBillId=${obj[10]}">View/</a></td>
			<!--  
			<td class="rowTotalValue"><input type="checkbox" class="checkbox" name="checked_bill" value="${obj[10]}"/></td>
			-->
		</tr>

	</c:forEach>
	<tr>
		<td class="rowTotalValue" colspan="6"><b style="color: blue;font-size: 14px;">TOTAL</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${insuranceDueAmount}</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${patientDueAmount}</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${totalAmountReceived}</b></td>		
		<td class="rowTotalValue"><b style="color: red;font-size: 14px;"><u>${totalAmount}</u></b></td>
	</tr>
</table>
<br />
<br />
</form>
</div> 
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>
