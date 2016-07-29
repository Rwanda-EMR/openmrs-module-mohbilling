<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>Department Management</h2>

<a href="department.form">Add new Departement</a>
<br/><br/>

<b class="boxHeader">Current Departement</b>
<div class="box">
<table>
	<tr>
		<th>#.</th>
		<th>Name</th>
		<th>Description</th>
		<th>Services</th>
		<th>service</th>
	</tr>
	<c:forEach items="${departments}" var="department" varStatus="status">	
		<tr>
			<td>${status.count}</td>
			<td><a href="department.form?departmentId=${department.departmentId}">${department.name}</a></td>			
			<td>${department.description}</td>			
			<td> &nbsp;<a href="serviceCategory.form?departmentId=${department.departmentId}">Add /<ahref="services.list?departmentId=${department.departmentId}">view</a></a></td>	
		
		</tr>
	</c:forEach>
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>