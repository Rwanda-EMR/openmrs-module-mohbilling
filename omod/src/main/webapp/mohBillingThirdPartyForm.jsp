<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="Manage Third Party" otherwise="/login.htm" redirect="/module/@MODULE_ID@/thirdParty.form" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<script type="text/javascript">
		var $bill = jQuery.noConflict();
		
		// Checking whether the User wants to delete the data
		function submitData(servId){
			var serv = "#deleteCtrl_"+servId
			var hrefValue = "thirdParty.form?deleteThirdParty=true&deleteThirdPartyId=" + servId;
			
			if(confirm("<spring:message code='@MODULE_ID@.general.delete.confirm'/>"))
				$bill(serv).attr("href", hrefValue);
		}

		$bill(document).ready(function(){
			
		});
</script>

<h2><spring:message code="@MODULE_ID@.billing.thirdParty"/></h2>

<ul id="menu">

		<li class="<c:if test='<%= request.getRequestURI().contains("InsuranceForm")%>'> active</c:if>">
			<a href="insurance.form">Add an Insurance</a>
		</li>
		<li class="<c:if test='<%= request.getRequestURI().contains("Third")%>'> active</c:if>">
			<a href="thirdParty.form">Manage Third Parties</a>
		</li>
		
</ul>

<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.create"/></b>
		
<div class="box">
	<div>
		<form action="thirdParty.form?save=true" method="post" id="form_third_party_bill">
			<div style="max-width: 99%; overflow: auto;">
				<input type="hidden" name="thirdPartyId" value="${thirdPartyId}"/>
				<table>
					<tr>
						<td class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.name"/></td>
						<td class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.rate"/></td>
						<td class="columnHeader"></td>
					</tr>
					<tr>
						<td><input size="60" type="text" name="thirdPartyName" value="${thirdParty.name}"/></td>
						<td><input size="5" type="text" name="thirdPartyRate" value="${thirdParty.rate}"/></td>
						<td><input type="submit" value="Save Third Party" name="saveThirdParty" id="saveThirdParty" /></td>
					</tr>
				</table>
			</div>
		</form>
	</div>
</div>

<br/>
<b class="boxHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.list"/></b>
		
<!-- If no Third Parties don't display anything! -->
<c:if test="${!empty thirdParties}">

	<div class="box">
		<table width="99%">
			<tr>
				<th class="columnHeader">#</th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.name"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.rate"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.creator"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.billing.thirdParty.createdDate"/></th>
				<th class="columnHeader"></th>
			</tr>
			
			<c:forEach items="${thirdParties}" var="thirdParty" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${thirdParty.name}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${thirdParty.rate}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${thirdParty.creator}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<openmrs:formatDate date="${thirdParty.createdDate}" type="string"/>
					</td>
					
					<td class="rowValue ${status.count%2!=0?'even':''}">
						<a href="thirdParty.form?editThirdParty=true&editThirdPartyId=${thirdParty.thirdPartyId}">
							<spring:message code='@MODULE_ID@.general.edit'/>
						</a> | <a id="deleteCtrl_${thirdParty.thirdPartyId}" onclick="submitData(${thirdParty.thirdPartyId});" href="#">
							<spring:message code='@MODULE_ID@.general.delete'/>
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>

</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>