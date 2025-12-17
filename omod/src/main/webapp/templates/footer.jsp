<openmrs:globalProperty var="hideLegacyUiHeaderAndFooter" key="billing.hideLegacyUiHeaderAndFooter"  />
<c:if test="${hideLegacyUiHeaderAndFooter != 'true'}">
	<%@ include file="/WEB-INF/template/footer.jsp"%>
</c:if>
