<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%><br />
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<b class="boxHeader">Department</b>
<div class="box">
	<table width="100%">
		<tr>
			<td>Department Name</td>
			<td> : <b>${department.name }</b></td>
			<td>Description</td>
			<td> : <b>${department.description }</b></td>
		</tr>
		<tr>
			<td>Created By</td>
			<td> : <b>${department.creator }</b></td>
			<td>Created Date</td>
			<td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${department.createdDate}" /></b></td>
			
		</tr>
	</table>
</div>
<br/>

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
<%@ include file="/WEB-INF/template/footer.jsp"%>