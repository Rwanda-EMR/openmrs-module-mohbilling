<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>


<h2>New Department</h2>

<p><a href="departments.list">View Current Department</a> </p>

<!-- div: creating new departiemt -->
<div class="box">
	<form action="department.form?departmentId=${department.departmentId}&save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="departmentName"  value="${department.name}"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea name="departmentDescription"  cols="70" rows="5">${department.description}</textarea></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Save Department"/></td>
			</tr>
		</table>
	</form>
</div>



<%@ include file="/WEB-INF/template/footer.jsp"%>