<%@page import="java.util.Date"%>
<%@page import="org.openmrs.module.mohbilling.model.BillableService"%>
<%@page import="org.openmrs.module.mohbilling.businesslogic.InsuranceUtil"%>
<%@page import="org.openmrs.module.mohbilling.model.ServiceCategory"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<script type="text/javascript" charset="utf-8">
$(document).ready(function(){
	  $("a").click(function() {
	       var tabId = this.id;
	       var index =  tabId.substring(16, tabId.indexOf('T')); 
	         $("#search_"+index).keyup(function (){
		       var $rows = $('#billableTable tr');
	      	   var val = $.trim($(this).val()).replace(/ +/g, ' ').toLowerCase();
	              $rows.show().filter(function() {
	                var text = $(this).text().replace(/\s+/g, ' ').toLowerCase();
	                return !~text.indexOf(val);
	              }).hide();  
	        }); 
	   });
});
</script> 


<style type="text/css">
	.tableList{
	
		margin-top: 10px;
		margin-left: auto;
		margin-right: auto;
		padding-top: 5px;
		border-top: 1px solid #dddddd;
		
	}

	.inTable{

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
		width:auto;
		
	}
	
	.unselectedService{
		background-color: #8FABC7;
		
	}
	
	.inTable:hover{
		color: #000000;
		background-color: #FF6400;
	}
	 /* to split billable services list into muliple columns */
    #container {
    /*column-count:2;
    -moz-column-count:2;
    -webkit-column-count:2;*/
    
    -webkit-column-count: 5; /* Chrome, Safari, Opera */
    -moz-column-count: 5; /* Firefox */
    column-count: 5;
   
   -webkit-column-width: 205px; /* Chrome, Safari, Opera */
    -moz-column-width: 205px; /* Firefox */
    column-width: 205px;

 

}
.inTable {
    -webkit-column-span: all; /* Chrome, Safari, Opera */
    column-span: all;
}

</style>

<div class="tableList">
		
	<%
			try {

				if (null != request.getParameter("serviceCategoryId")) {

					ServiceCategory sc = InsuranceUtil
							.getValidServiceCategory(Integer.valueOf(request
									.getParameter("serviceCategoryId")));
					
					List<BillableService> billableServices = InsuranceUtil
							.getBillableServicesByServiceCategory(sc,
									new Date(), false);

					List<String> bServ = new ArrayList<String>();

					if (billableServices == null
							|| billableServices.size() == 0)
						out.println("<center>No  services corresponding to this category found !</center>");
					else {
						
						out.println("<input type='text' id='search_"+sc.getServiceCategoryId()+"' placeholder='Type to search'>");
						out.println("<div id='container'>");
						out.println("<table id='billableTable'>");
						out.println("<tbody>");
							for (BillableService bs : billableServices) {
								if(!bs.isRetired())
									out.println("<tr>");	
								    out.println("<td class='submenu'>");
									out.println("<div class='inTable unselectedService' id='billableService_"+bs.getServiceId()+"' onclick=addServiceToCart('"+bs.getServiceId()+"','"+bs.getFacilityServicePrice().getName().replace("'","&nbsp;").replace(" ","&nbsp;")+"','"+bs.getMaximaToPay()+"')>"
											+ bs.getFacilityServicePrice().getName()
										+ "</div>");
									out.println("</td>");
									out.println("</tr>");
							}
							out.println("</tbody>");
						out.println("</table>");
						out.println("</div>");
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		%>
</div>