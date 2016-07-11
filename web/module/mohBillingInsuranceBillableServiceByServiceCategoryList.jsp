<%@page import="java.util.Date"%>
<%@page import="org.openmrs.module.mohbilling.model.BillableService"%>
<%@page import="org.openmrs.module.mohbilling.businesslogic.InsuranceUtil"%>
<%@page import="org.openmrs.module.mohbilling.model.ServiceCategory"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<script type="text/javascript">
$(document).ready(function(){
	var $rows = $('#table tr');
	$('#search').keyup(function() {
	    var val = $.trim($(this).val()).replace(/ +/g, ' ').toLowerCase();
	    $rows.show().filter(function() {
	        var text = $(this).text().replace(/\s+/g, ' ').toLowerCase();
	        return !~text.indexOf(val);
	    }).hide();
	});
});
</script>

<style>
	.tableList{
		width: 97%;
		margin-top: 5px;
		margin-left: auto;
		margin-right: auto;
		padding-top: 5px;
		border-top: 1px solid #dddddd;
	}

	.inTable{
		max-width: 350px;
		min-width: 60px;
		margin: 1px;
		padding: 2px;
		cursor: pointer;
		-moz-border-radius: 4px;
		display: inline-block;
		border-right: 2px solid #dddddd;
		border-bottom: 2px solid #dddddd;
		color: #ffffff;
		font-weight: bold;
		font-size: 0.8em;
		text-transform: uppercase;
	}
	
	.unselectedService{
		background-color: #8FABC7;
	}
	
	.inTable:hover{
		color: #000000;
		background-color: #FF6400;
	}
</style>

<div class="tableList">
		
	<%
			try {

				if (null != request.getParameter("serviceCategoryId")) {

					ServiceCategory sc = InsuranceUtil
							.getValidServiceCategory(Integer.valueOf(request
									.getParameter("serviceCategoryId")));
					out.println("<input type='text' id='search' placeholder='Type to search'>");
					
					List<BillableService> billableServices = InsuranceUtil
							.getBillableServicesByServiceCategory(sc,
									new Date(), false);

					List<String> bServ = new ArrayList<String>();

					if (billableServices == null
							|| billableServices.size() == 0)
						out.println("<center>No  services corresponding to this category found !</center>");
					else {
						out.println("<table id='table'>");
							for (BillableService bs : billableServices) {
								if(!bs.isRetired())
									out.println("<tr>");	
								    out.println("<td>");
									out.println("<input type='checkbox' class='inTable unselectedService' id='billableService_"+bs.getServiceId()+"' onclick=addServiceToCart('"+bs.getServiceId()+"','"+bs.getFacilityServicePrice().getName().replace("'","&nbsp;").replace(" ","&nbsp;")+"','"+bs.getMaximaToPay()+"')>"
											+ bs.getFacilityServicePrice().getName()
											+ "</input>");
									out.println("</td>");
									out.println("</tr>");
							}
							out.println("</table>");
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		%>
</div>