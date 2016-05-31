<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!--  
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
-->

<!-- script to create a pop up windows for unpaid bills -->
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_style.css" /> 


<%@ include file="templates/mohBillingLocalHeader.jsp"%>


<%@ include file="templates/mohBillingAdminHeader.jsp"%>



<%@ include file="/WEB-INF/template/footer.jsp"%>
