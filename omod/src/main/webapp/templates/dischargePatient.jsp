<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-3.5.1.js" />
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-ui.js" />
<openmrs:htmlInclude file="/moduleResources/mohbilling/jquery-ui.css" />
<%@ page import = "java.util.Date" %>
<%@ page import = "java.text.SimpleDateFormat" %>
<%
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String currDate = sdf.format(new Date());
%>

<style>
    .ui-widget-header,.ui-state-default, ui-button{
        background:lightseagreen;
        color: white;
        border: 1px solid #20B2AA;
        font-weight:  bold;
    }
    #invalid{
        color: red;
        font-size: large;
        font-weight: bold;
    }
</style>

<script>
    $(function(){
        $("#discharge").dialog({
            autoOpen: false,
            resizeable: false,
            buttons: {
                Cancel: function() {$(this).dialog("close");},
            },
            position: {
                my: "center",
                at: "center"
            },
            title: "Discharge Patient",
            height: 450,
            width: 650
            /*hide: "slide",
            show: "slide"*/
        });
        $("#btn").on("click",function(){
            $("#discharge").dialog("open");
        });
        $("#discBill").on("click",function (event){
            if ( $("#closeGlobalBill option:selected").val()!="" ){
                $("#closeGB").submit();
                $("#discharge").dialog("close");
            }
            $("#invalid").text("Please!Select Closing Reason.").show().fadeOut(4000);
            event.preventDefault();
        });
    });
</script>
<div id="discharge">
    <form action="consommation.list?globalBillId=${globalBill.globalBillId}&edit=true&discharge=${discharge}" method="post" id="closeGB">
        <span id="close"></span>
        <table>
            <tr>
                <td style="font-size:15px"><b>Patient</b> </td>
                <td><input type="text" readonly="readonly" name="patientName"
                           value="${insurancePolicy.owner.personName}" size="40" /></td>
            </tr>
            <tr></tr>
            <tr></tr>
            <tr>
                <td style="font-size:15px"><b>Admission Date</b></td>
                <td><openmrs_tag:dateField formFieldName="admissionDate" startValue="${globalBill.admission.admissionDate}" /> </td>
            </tr>
            <tr></tr>
            <tr></tr>
            <tr>
                <td style="font-size:15px"><b>Closing Date</b></td>
                <td><input type="text" readonly="readonly" value="<%=currDate%>" size="40"/></td>
            </tr>
            <tr></tr>
            <tr></tr>
            <tr>
                <td style="font-size:15px"><b>Closing Reason</b> </td>
                <td>
                    <select name="closeGBReason" id="closeGlobalBill">
                        <option value="">           </option>
                        <option value="Billing Completed">Billing Completed</option>
                    </select>
                <td>
            </tr>
            <tr><td></td><td><span id="invalid"></span></td></tr>
            <tr></tr>
            <tr>
                <td></td>
                <td>
                    <button id="discBill">Close Bill</button>
                </td>
            </tr>
        </table>
    </form>
</div>