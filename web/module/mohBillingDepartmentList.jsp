<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>Department Management</h2>

<a href="department.form">Add new Departement</a>
<br/><br/>

<b class="boxHeader">Current Departement</b>
<div class="box">
<table style="width:70%">
	<tr>
		<th style="width:3%">#.</th>
		<th style="width:10%">Name</th>
		<th style="width:10%">Description</th>
		<th style="width:3%">Services</th>
	</tr>
	 <c:set var="count" value="0" />
	<c:forEach items="${departments}" var="department" varStatus="status">
		<tr>
			<td>${status.count}</td>
			<td><a href="department.form?departmentId=${department.departmentId}">${department.name}</a></td>			
			<td>${department.description}</td>			
			<td> &nbsp;<a href="services.list?departmentId=${department.departmentId}">view</a></a></td>
	 <c:set var="temp" value="0" />
	    <c:if test="${department.departmentId == temp}">
            ${rate}
        </c:if>
		<tr >
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="department.form?departmentId=${department.departmentId}">${department.name}</a></td>			
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${department.description}</td>		
			<td class="rowValue ${(status.count%2!=0)?'even':''}">(2)
			 <a href="services.list?departmentId=${department.departmentId}">view</a>
			</td>
			</td>	
		</tr>
	</c:forEach>
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>