<openmrs:globalProperty var="hideLegacyUiHeaderAndFooter" key="billing.hideLegacyUiHeaderAndFooter"  />
<c:choose>
	<c:when test="${hideLegacyUiHeaderAndFooter == 'true'}">
		<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>
		<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
	</c:when>
	<c:otherwise>
		<%@ include file="/WEB-INF/template/header.jsp"%>
	</c:otherwise>
</c:choose>
