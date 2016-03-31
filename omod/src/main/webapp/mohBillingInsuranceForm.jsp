<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:require privilege="Add Insurance" otherwise="/login.htm" redirect="/module/@MODULE_ID@/insurance.list" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.insurance.${(param.identifierId ne null)?'edit':'new'}" /></h2>
<openmrs:hasPrivilege privilege="View Current Insurances">
<a href="insurance.list">View Current Insurances</a> </openmrs:hasPrivilege>
<br/><br/>

<div class="box">
	<form action="insurance.form?save=true" method="post">
		<table>
			<c:if test="${param.insuranceId ne null}">
				<tr>
					<td></td>
					<td><input type="hidden" name="insuranceId" value="${insurance.insuranceId}"/></td>
				</tr>
			</c:if>
			<tr>
				<td>Name</td>
				<td><input autocomplete="off" type="text" name="insuranceName" size="40" value="${insurance.name}"/></td>
			</tr>
			<tr>
				<td>Insurance Category</td>
				<td>
					<select name="insuranceCategory">
						<c:forEach items="${insuranceCategories}" var="insuranceCategory">
							<option value="${insuranceCategory}" <c:if test='${insurance.category==insuranceCategory}'>selected='selected'</c:if>>${insuranceCategory}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>Related Concept</td>
				<td><openmrs_tag:conceptField formFieldName="insuranceRelatedConceptId" initialValue="${insurance.concept.conceptId}" /></td>
			</tr>
			<tr>
				<td>Address</td>
				<td><textarea cols="30" rows="3" name="insuranceAddress">${insurance.address}</textarea></td>
			</tr>
			<tr>
				<td>Phone</td>
				<td><input autocomplete="off" type="text" name="insurancePhone" size="25" value="${insurance.phone}"/></td>
			</tr>
			<tr>
				<td colspan="2">
					<hr/>
					<table>
						<tr>
							<td colspan="2"><b>Insurance rate</b></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
						</tr>
						<tr>
							<td>Rate</td>
							<td><input autocomplete="off" type="text" name="insuranceRate" size="5" value="${insurance.currentRate.rate}" class="numbers"/>%</td>
							<td>Flat Fee</td>
							<td><input autocomplete="off" type="text" name="insuranceFlatFee" size="5" value="${insurance.currentRate.flatFee}" class="numbers"/>Frw</td>
							<td>Start date</td>
							<td><input autocomplete="off" type="text" name="insuranceRateStartDate" size="11" value="<openmrs:formatDate date="${insurance.currentRate.startDate}" type="string"/>" onclick="showCalendar(this);"/></td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td><c:if test="${param.id ne null}"><input type="hidden" value="${param.id}"/></c:if></td>
				<td><input type="submit" value="Save Insurance"/></td>
			</tr>
		</table>
	</form>
</div>

<c:if test="${param.insuranceId!=null}">
	<br/>
	<div class="box">
		<form action="insurance.form?void=true" method="post">
			<b>Void this Insurance</b>
			<table>
				<tr>
					<td>Void Reason</td>
					<td><input type="text" name="voidReason" size="50" value="${insurance.voidReason}"/></td>
				</tr>
				<tr>
					<td><input type="hidden" value="${param.insuranceId}" name="insuranceId"/></td>
					<td><input type="submit" value="Void Insurance"/></td>
				</tr>
			</table>
		</form>
	</div>

</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
