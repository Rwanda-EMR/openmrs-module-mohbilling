<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>New Hospital Service</h2>

<p><a href="services.list">View Current Hospital Services</a> </p>

<div class="box">
	<form action="service.form?serviceId=${service.serviceId }&save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="serviceName" size="30" value="${service.name }"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea name="description" cols="70" rows="5">${service.description}</textarea></td>
			</tr>			
			<tr>
				<td></td>
				<td><input type="submit" value="Save Hop Service"/></td>
			</tr>
		</table>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>