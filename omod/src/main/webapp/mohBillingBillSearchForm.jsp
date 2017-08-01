
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2>Manage Bill Payments</h2>

 
<b class="boxHeader">Search Global Bill </b>
<div class="box">
<form action="billSearch.form" method="get">
		<table>
			<tr>
				<td>Global Bill Identifier</td>
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
<br>
<b class="boxHeader">Search Consommation </b>
<div class="box">
<form action="" method="get">
		<table>
			<tr>
				<td>Consommation Identifier</td>
				<td><input type="text" name="consommationId"/></td>
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





