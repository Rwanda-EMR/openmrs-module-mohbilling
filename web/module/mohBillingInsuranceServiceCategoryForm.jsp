<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.insurance.service.category.manage" /></h2>

<a href="insuranceServiceCategory.list?insuranceId=${insurance.insuranceId}"><b>${insurance.name}</b> : <spring:message code="@MODULE_ID@.insurance.service.category.current" /></a>
<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.insurance.company.title" /></b>
<div class="box">
	<table width="100%">
		<tr>
			<td>Insurance Name</td>
			<td> : <b>${insurance.name}</b></td>
			<td>Related Concept</td>
			<td> : <b>${insurance.concept.name}</b></td>
		</tr>
		<tr>
			<td>Insurance Rate</td>
			<td> : <b>${insurance.currentRate.rate} %</b></td>
			<td>Flat Fee</td>
			<td> : <b>${insurance.currentRate.flatFee} Rwf</b></td>
		</tr>
	</table>
</div>
<br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.insurance.service.category.new" /></b>
<div class="box">
	<form action="insuranceServiceCategory.form?insuranceId=${insurance.insuranceId}&save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input autocomplete="off" type="text" name="serviceCategoryName" size="70"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea name="serviceCategoryDescription" cols="30" rows="3"></textarea></td>
			</tr>
			<!-- <tr>
				<td>Price</td>
				<td><input autocomplete="off" type="text" name="serviceCategoryPrice" size="5" class="numbers"/> Rwf</td>
			</tr> -->
			<tr>
				<td></td>
				<td><input type="submit" value="Save Insurance Service Category"/></td>
			</tr>
		</table>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>