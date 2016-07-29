
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.billing" /></h2>

 
<b class="boxHeader">Search Bill by BIll Identifier </b>
<div class="box">
<form action="billSearch.form" method="get">
		<table>
			<tr>
				<td>Bill Identifier Id</td>
				<td><input type="text" name="billIdentifier"/></td>
				<td></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Search"/></td>
				<td></td>
			</tr>
		</table>
</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>





