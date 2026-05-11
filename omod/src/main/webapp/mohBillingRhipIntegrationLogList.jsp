<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<openmrs:require privilege="Billing Configuration - View Billing Admin" otherwise="/login.htm" redirect="/module/@MODULE_ID@/rhipIntegrationLogs.form" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>RHIP Integration Logs</h2>
<p>
	Showing <b>${fn:length(logs)}</b> of <b>${totalCount}</b> RHIP calls. Access is restricted to super user accounts.
</p>

<style>
	.rhip-log-filter {
		border: 1px solid #d8d8d8;
		padding: 10px;
		margin: 10px 0 14px 0;
	}
	.rhip-log-filter table td {
		padding: 4px 8px 4px 0;
	}
	.rhip-log-filter input,
	.rhip-log-filter select {
		width: 150px;
	}
	.rhip-log-filter .wide {
		width: 260px;
	}
	.rhip-log-pagination {
		margin: 10px 0;
	}
	.rhip-log-pagination a,
	.rhip-log-pagination span {
		margin-right: 10px;
	}
</style>

<form method="get" action="rhipIntegrationLogs.form" class="rhip-log-filter">
	<table>
		<tr>
			<td>Start date</td>
			<td><input type="text" name="startDate" value="${startDate}" placeholder="yyyy-MM-dd" /></td>
			<td>End date</td>
			<td><input type="text" name="endDate" value="${endDate}" placeholder="yyyy-MM-dd" /></td>
			<td>Sender</td>
			<td><input type="text" name="senderUsername" value="${senderUsername}" /></td>
		</tr>
		<tr>
			<td>Operation</td>
			<td>
				<select name="operationType">
					<option value="" <c:if test="${empty operationType}">selected="selected"</c:if>>All</option>
					<option value="VOUCHER_SUBMIT" <c:if test="${operationType eq 'VOUCHER_SUBMIT'}">selected="selected"</c:if>>VOUCHER_SUBMIT</option>
					<option value="PRACTITIONER_DETAILS" <c:if test="${operationType eq 'PRACTITIONER_DETAILS'}">selected="selected"</c:if>>PRACTITIONER_DETAILS</option>
					<option value="PRACTITIONER_CREATE" <c:if test="${operationType eq 'PRACTITIONER_CREATE'}">selected="selected"</c:if>>PRACTITIONER_CREATE</option>
					<option value="PRACTITIONER_TYPES" <c:if test="${operationType eq 'PRACTITIONER_TYPES'}">selected="selected"</c:if>>PRACTITIONER_TYPES</option>
				</select>
			</td>
			<td>Status</td>
			<td>
				<select name="responseStatus">
					<option value="" <c:if test="${empty responseStatus}">selected="selected"</c:if>>All</option>
					<option value="SUCCESS" <c:if test="${responseStatus eq 'SUCCESS'}">selected="selected"</c:if>>SUCCESS</option>
					<option value="ERROR" <c:if test="${responseStatus eq 'ERROR'}">selected="selected"</c:if>>ERROR</option>
					<option value="UNKNOWN" <c:if test="${responseStatus eq 'UNKNOWN'}">selected="selected"</c:if>>UNKNOWN</option>
					<option value="NO_RESPONSE" <c:if test="${responseStatus eq 'NO_RESPONSE'}">selected="selected"</c:if>>NO_RESPONSE</option>
				</select>
			</td>
			<td>HTTP</td>
			<td><input type="text" name="responseCode" value="${responseCode}" /></td>
		</tr>
		<tr>
			<td>Text</td>
			<td colspan="3"><input class="wide" type="text" name="query" value="${query}" /></td>
			<td>Page size</td>
			<td>
				<select name="pageSize">
					<option value="25" <c:if test="${pageSize eq 25}">selected="selected"</c:if>>25</option>
					<option value="50" <c:if test="${pageSize eq 50}">selected="selected"</c:if>>50</option>
					<option value="100" <c:if test="${pageSize eq 100}">selected="selected"</c:if>>100</option>
					<option value="200" <c:if test="${pageSize eq 200}">selected="selected"</c:if>>200</option>
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="6">
				<input type="submit" value="Filter" />
				<a href="rhipIntegrationLogs.form">Clear filters</a>
			</td>
		</tr>
	</table>
</form>

<div class="rhip-log-pagination">
	<c:choose>
		<c:when test="${hasPreviousPage}">
			<a href="rhipIntegrationLogs.form?page=${previousPage}&${filterQueryString}">Previous</a>
		</c:when>
		<c:otherwise><span>Previous</span></c:otherwise>
	</c:choose>
	<span>Page ${page} of ${totalPages}</span>
	<c:choose>
		<c:when test="${hasNextPage}">
			<a href="rhipIntegrationLogs.form?page=${nextPage}&${filterQueryString}">Next</a>
		</c:when>
		<c:otherwise><span>Next</span></c:otherwise>
	</c:choose>
</div>

<c:choose>
	<c:when test="${empty logs}">
		<p>No RHIP integration logs found.</p>
	</c:when>
	<c:otherwise>
		<table cellpadding="3" cellspacing="0" width="100%">
			<thead>
			<tr>
				<th class="columnHeader">Date</th>
				<th class="columnHeader">Sender</th>
				<th class="columnHeader">Operation</th>
				<th class="columnHeader">Status</th>
				<th class="columnHeader">HTTP</th>
				<th class="columnHeader">Endpoint</th>
				<th class="columnHeader">Payload</th>
				<th class="columnHeader">Response</th>
				<th class="columnHeader">Error</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${logs}" var="log" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<fmt:formatDate value="${log.dateCreated}" pattern="yyyy-MM-dd HH:mm:ss"/>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${log.senderUsername}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${log.operationType}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${log.responseStatus}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${log.responseCode}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}" style="max-width: 260px; word-break: break-all;">${log.endpointUrl}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<textarea rows="4" cols="40" readonly="readonly">${log.requestPayload}</textarea>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<textarea rows="4" cols="40" readonly="readonly">${log.responseBody}</textarea>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<textarea rows="4" cols="30" readonly="readonly">${log.errorMessage}</textarea>
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</c:otherwise>
</c:choose>

<div class="rhip-log-pagination">
	<c:choose>
		<c:when test="${hasPreviousPage}">
			<a href="rhipIntegrationLogs.form?page=${previousPage}&${filterQueryString}">Previous</a>
		</c:when>
		<c:otherwise><span>Previous</span></c:otherwise>
	</c:choose>
	<span>Page ${page} of ${totalPages}</span>
	<c:choose>
		<c:when test="${hasNextPage}">
			<a href="rhipIntegrationLogs.form?page=${nextPage}&${filterQueryString}">Next</a>
		</c:when>
		<c:otherwise><span>Next</span></c:otherwise>
	</c:choose>
</div>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
