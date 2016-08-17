<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>Services management</h2>

<a href="service.form">Add new Service</a>
<br/><br/>

<b class="boxHeader">Current Services </b>
<div class="box">
<form action="services.list?department=${department.departmentId }">
<table style="width:100%">
	<tr>
		<th  style="width:5%">#.</th>
		<th style="width:8%">Name</th>
		<th style="width:8%">Description</th>		
	</tr>
	<c:forEach items="${services}" var="service" varStatus="status">
			<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="service.form?serviceId=${service.serviceId}">${service.name}</a></td>			
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${service.description}</td>			
		</tr>
	</c:forEach>
</table>
</form>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>