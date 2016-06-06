<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<script type="text/javascript">
	$(document).ready(function() {
		$(".list_section").show();
		$(".create_section").hide();
		
		$(".new_department").click(function(){
		    $(".create_section").show();
		    $(".list_section").hide();
		});

	});
</script>

<h2>Manage Department</h2>
<p class="new_department"><a>Add New Department</a> </p>

<!-- div: creating new departiemt -->
<div class="create_section">
	<form action="department.form?insuranceId=1&save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="departmentName" size="70"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea name="departmentDescription" cols="30" rows="3"></textarea></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Save Department"/></td>
			</tr>
		</table>
	</form>
</div>

<br/><br/>
<!-- div: list current departiemts -->
<div class="list_section">
<b class="boxHeader">Current Departments</b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Name</th>
			<th class="columnHeader">Description</th>
			<th class="columnHeader">Service Categories</th>
			<th class="columnHeader"></th>
		</tr>
		<c:if test="${empty facilityServices}"><tr><td colspan="9"><center>No Facility Services found !</center></td></tr></c:if>
		<c:forEach items="${facilityServices}" var="facilityService" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}. </td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="facilityService.form?facilityServiceId=${facilityService.facilityServicePriceId}">${facilityService.name}</a></td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${facilityService.description}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					${facilityService.category}
					<c:if test="${empty facilityService.category}">
						<a style="color: red;" href="facilityService.list?facilityServiceId=${facilityService.facilityServicePriceId}">ADD CATEGORY</a>
					</c:if>
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<a href="facilityServiceByInsuranceCompany.list?facilityServiceId=${facilityService.facilityServicePriceId}">Details</a>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>