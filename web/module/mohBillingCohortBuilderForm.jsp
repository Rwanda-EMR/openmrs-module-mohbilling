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
	    <li>
			<a href="received.form"><spring:message code="@MODULE_ID@.billing.received"/></a>
		</li>
		 <li>
			<a href="recettes.form"><spring:message code="@MODULE_ID@.billing.revenue"/></a>
		</li>
		<li>
			<a href="facture.form"><spring:message code="@MODULE_ID@.billing.facture"/></a>
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
<b class="boxHeader">Search results  <input id="print_button" type="submit" value="PRINT" onclick=""/>
	<div style="float:right"><a style="" href="cohort.form?print=true&patientId=${patientId}&startDate=${startDate}&endDate=${endDate}&insurance=${insurance.insuranceId}&serviceId=${serviceId}"><b style="color: red;font-size: 14px;">Print PDF</b></a></div>
</b>


<div class="printarea" ">

<div class="meta">
<span style="float: left"><b>REPUBLIQUE DU RWANDA</b></span><span style="float: right;">Printed on: <openmrs:formatDate date="${today}" type="short" /></span><br />
<img src="${healthFacilityLogo}" height="90" width="90"><br />
<b>${healthFacilityName}</b><br />

<c:if test="${not empty address}">
	<b>${address}</b><br />
</c:if>

<c:if test="${not empty healthFacilityShortCode}">
	<b>Short code: ${healthFacilityShortCode}</b><br />
</c:if>

<c:if test="${not empty healthFacilityEmail}">
	<b>Email: ${healthFacilityEmail}</b><br />
</c:if>

</div>
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
			
		</tr>

	</c:forEach>
	<tr>
		<td class="rowTotalValue" colspan="6"><b style="color: blue;font-size: 14px;">TOTAL</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${insuranceDueAmount}</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${patientDueAmount}</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;">${totalAmountReceived}</b></td>		
		<td class="rowTotalValue"><b style="color: red;font-size: 14px;"><u>${totalAmount}</u></b></td>
		<td class="rowTotalValue"></td>
	</tr>
</table>
<br />
<br />

<div class="meta">
	<c:if test="${not empty patientNames}">
		<span style="float: left;"><b>Signature du patient: <br />${patientNames}</b></span>
	</c:if>
	<span style="float: right;"><b>Noms et Signature du Caissier: <br />${cashierNames}</b></span>
</div>

</div>

</div> 
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>
