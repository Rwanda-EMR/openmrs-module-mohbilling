<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>Manage services</h2>

<!-- div: creating new Service -->
<div >
	<form action="service.form?save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="serviceName" size="70"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea name="description" cols="30" rows="3"></textarea></td>
			</tr>			
			<tr>
				<td></td>
				<td><input type="submit" value="Save Hop Service"/></td>
			</tr>
		</table>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>