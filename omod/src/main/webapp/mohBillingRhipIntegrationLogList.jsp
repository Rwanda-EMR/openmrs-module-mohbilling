<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<openmrs:require privilege="Billing Configuration - View Billing Admin" otherwise="/login.htm" redirect="/module/@MODULE_ID@/rhipIntegrationLogs.form" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>RHIP Integration Logs</h2>
<p>
	Showing latest <b>${limit}</b> RHIP calls. Access is restricted to super user accounts.
</p>

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

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
