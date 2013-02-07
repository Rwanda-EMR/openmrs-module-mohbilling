<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.insurance.service.category.manage" /></h2>

<a href="insuranceServiceCategory.form?insuranceId=${insurance.insuranceId}"><spring:message code="@MODULE_ID@.insurance.service.category.add" /></a>
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

<b class="boxHeader"><u>${insurance.name}</u> : <spring:message code="@MODULE_ID@.insurance.service.category.current" /></b>
<div class="box">
	<table width="99%">
		<tr>
			<td class="columnHeader"></td>
			<td class="columnHeader">Name</td>
			<td class="columnHeader">Description</td>
			<!-- <td class="columnHeader">Price</td> -->
		</tr>
		<c:if test="${empty insurance.categories}"><tr><td colspan="4"><center>No Insurance Service Category found !</center></td></tr></c:if>
		<c:forEach items="${insurance.categories}" var="isc" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${isc.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${isc.description}</td>
				<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}">${isc.price} Rwf</td> -->
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>