<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%><br />

<b class="boxHeader">Add Service Category</b>
<div class="box">
	<form
		action="serviceCategory.form?departmentId=${departmentId}&save=true"
		method="post">
		<table>
			<tr>
				<td>Service Name</td>
				<td><select name="serviceId">
						<c:forEach items="${services}" var="service">
							<option value="${service.serviceId}">${service.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			
			<tr>
				<td></td>
				<td><input type="submit" value="Save Service Category" /></td>
			</tr>
		</table>
	</form>
</div>
Service name :${service.name}
<%@ include file="/WEB-INF/template/footer.jsp"%>