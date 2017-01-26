<%@page import="java.util.Date"%>
<%@page import="org.openmrs.module.mohbilling.model.BillableService"%>
<%@page import="org.openmrs.module.mohbilling.businesslogic.InsuranceUtil"%>
<%@page import="org.openmrs.module.mohbilling.model.ServiceCategory"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<script type="text/javascript" charset="utf-8">
$j(document).ready(function(){
	  $j("a").click(function() {
		  /* get the link id */
	       var tabId = this.id;
		  /* get the substring to retrieve the number inside the string */
	       var index =  tabId.substring(16, tabId.indexOf('T')); 
	          $j("#search_"+index).keyup(function (){
		       var $rows = $j('#itemList div');
	      	   var val = $.trim($j(this).val()).replace(/ +/g, ' ').toLowerCase();
	              $rows.show().filter(function() {
	                var text = $j(this).text().replace(/\s+/g, ' ').toLowerCase();
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
		float: left;
		
	}
	
	.unselectedService{
		background-color: #8FABC7;
		
	}
	
	.inTable:hover{
		color: #000000;
		background-color: #FF6400;
	}
	 /* to split billable services list into muliple columns */
    /* #container {
    /*column-count:2;
    -moz-column-count:2;
    -webkit-column-count:2;*/
    
    -webkit-column-count: 5; /* Chrome, Safari, Opera */
    -moz-column-count: 5; /* Firefox */
    column-count: 5;
    
} */

.item{
    margin:0 0 2em 2em;
    list-style-type: decimal;
    float:left;
}
.item{
    -webkit-column-break-inside:avoid;
    -moz-column-break-inside:avoid;
    -o-column-break-inside:avoid;
    -ms-column-break-inside:avoid;
    column-break-inside:avoid;
}
#itemList{
    -webkit-column-count: 5; -webkit-column-gap:2em;
    -moz-column-count:5; -moz-column-gap:2em;
    -o-column-count:5; -o-column-gap:2em;
    column-count:5; column-gap:2em;
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
						//out.println("<div id='container'>");
						//out.println("<table id='billableTable'>");
						//out.println("<tbody>");
						out.println("<ul id='itemList'>");
							for (BillableService bs : billableServices) {
								//if(!bs.isRetired())
									/* out.println("<tr>");	
								    out.println("<td class='submenu'>"); */
								    out.println("<div class='inTable unselectedService' id='billableService_"+bs.getServiceId()+"' onclick=addServiceToCart('"+bs.getServiceId()+"','"+bs.getFacilityServicePrice().getName().replace("'","&nbsp;").replace(" ","&nbsp;")+"','"+bs.getMaximaToPay()+"')>");
									out.println("<li class='item'>"
											+ bs.getFacilityServicePrice().getName()
										+ "</li>");
									out.println("</div>");
									/* out.println("</td>");
									out.println("</tr>"); */
							}
							//out.println("</tbody>");
						//out.println("</table>");
						//out.println("</div>");
							out.println("</ul>");
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		%>
</div>