<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.PrintArea.js" />	
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<script type="text/javascript" language="JavaScript">
	var $ = jQuery.noConflict();

	$(document).ready(function() {
		$("input#print_button").click(function() {
			$("div.printarea").printArea();
		});	

		$("input#print_button").click(function() {
			$('#header').show();
			$("div.printarea").printArea();
			$('#header').hide();
		});	
	});

   
	
</script>




<ul id="menu">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.cohort"/></a>
		</li>
		
		<li class="<c:if test='<%= request.getRequestURI().contains("Recovery")%>'> active</c:if>">
			<a href="ReportRecovery.form"><spring:message code="@MODULE_ID@.billing.recovery"/></a>
		</li>	
</ul>
<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">


<form action="cohort.form" method="post" name="">

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
		<td><select name="insurance">
			<option selected="selected" value="${insurance.insuranceId}"> 
			<c:choose>
             <c:when test="${insurance.insuranceId != nul}">

			 ${insurance.name}
			 
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

	<tr>
		<td>Patient</td><td>
		<openmrs:fieldGen type="org.openmrs.Patient" formFieldName="patientId" val="" />
		 <input type="hidden" name="patientIdnew" value="${patientIdStr}"/>	
		</td>
	<!-- 	<td>Facility Services 
		<select name="serviceId">
		
		       <option value="">---Select---</option>
			<c:forEach items="${serviceCategory}" var="service">
				<option value="${service.serviceCategoryId}">${service.name}</option>
			</c:forEach>
		</select></td>  -->
	</tr>


</table>

<input type="submit" value="Search" />
</div>
<br>

<br>
</form>
<c:if test="${fn:length(billObj)!=0}">
<div><b class="boxHeader">Search results  <input id="print_button" type="submit" value="PRINT" onclick=""/> </b>

<div class="header" style="display: none;">  

  KPH

 </div>
<div class="printarea" ">

<table width="99%">
	<tr>
		<td>No</td>
		<td>Date</td>
		<td>Policy Id Number</td>
		<td>Beneficiary</td>

		<td>Billable Services</td>

		<td>Insurance Name</td>
		<td>Insurance due</td>
		<td>Patient Due : ${patientIdStr}</td>
		<td>Amount</td>
	</tr>

	<c:forEach items="${billObj}" var="obj" varStatus="status">
		<tr>
			<td class="rowValue">${status.count}</td>
			<td class="rowValue">${obj[0]}</td>
			<td class="rowValue">${obj[1]}</td>
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
			<td class="rowValue">${obj[5]}</td>
			<td class="rowValue">${obj[6]}</td>
			<td class="rowValue">${obj[7]}</td>
			
		</tr>

	</c:forEach>
	<tr>
	<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>
	<td class="rowValue"><b style="color: red">${totalAmount }</b></td>
	</tr>
</table>
</div>
</form>
</div> 
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>
