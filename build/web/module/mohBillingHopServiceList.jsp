<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>Services management</h2>

<a href="service.form">Add new Service</a>
<br/><br/>

<b class="boxHeader">Current Services </b>
<div class="box">
<table>
	<tr>
		<th>#.</th>
		<th>Name</th>
		<th>Description</th>	
		
	</tr>
	<c:forEach items="${services}" var="service" varStatus="status">
			<tr>
			<td>${status.count}</td>
			<td><a href="hopService.form?serviceId=${service.serviceId}">${service.name}</a></td>			
			<td>${service.description}</td>			
		</tr>
	</c:forEach>
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>