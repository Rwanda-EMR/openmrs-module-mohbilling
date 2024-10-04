<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<style>
    .pagination {
        display: flex;
        padding: 8px;
        justify-content: space-between;
        align-items: center;
    }

    .pagination a {
        color: black;
        float: left;
        padding: 8px 12px;
        text-decoration: none;
        border: 1px solid #ddd;
        margin: 0 4px;
        font-family: Arial, sans-serif;
        font-size: 14px;
        border-radius: 5px;
    }

    .pagination a:hover {
        background-color: #ddd;
    }

    .pagination strong.current-page {
        float: left;
        padding: 8px 16px;
        margin: 0 4px;
        border: 1px solid #4CAF50;
        color: white;
        background-color: #4CAF50;
        font-family: Arial, sans-serif;
        font-size: 14px;
        border-radius: 5px;
    }

    .pagination a:first-child {
        border-radius: 5px 0 0 5px;
    }

    .pagination a:last-child {
        border-radius: 0 5px 5px 0;
    }
</style>

<script>
    function goToPage(pageNumber) {
        // Set the page number in the form
        document.getElementById('pageNumber').value = pageNumber;
        document.getElementById('submitId').click();
    }

    function resetPage() {
        // Reset the page number to 1 when a new search is performed
        document.getElementById('pageNumber').value = 1;
    }
</script>

<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">
    <form method="post" action=" ">
        <input type="hidden" name="formStatus" id="formStatusId" value=""/>
        <input type="hidden" name="pageNumber" id="pageNumber" value="${currentPage}"/>
        <input type="hidden" name="pageSize" id="pageSize" value="${pageSize}"/>
        <table>
            <tr class="dates">
                <td>
                    <table>
                        <tr>
                            <td>Start Date:</td>
                            <td><input type="text" size="11"
                                       value="${startDate}" name="startDate"
                                       onclick="showCalendar(this)"/></td>
                            <td class="timelabel">hh</td>
                            <td>
                                <select name="startHour" class="time">
                                    <option value="00">00</option>
                                    <c:forEach var="counter" begin="1" end="9">
                                        <option value="0${counter}">0${counter}</option>
                                    </c:forEach>

                                    <c:forEach var="j" begin="10" end="23">
                                        <option value="${j}">${j}</option>
                                    </c:forEach>
                                </select>

                            </td>
                            <td class="timelabel">mm</td>
                            <td>
                                <select name="startMinute" class="time">
                                    <option value="00">00</option>
                                    <c:forEach var="i" begin="1" end="9">
                                        <option value="0${i}">0${i}</option>
                                    </c:forEach>

                                    <c:forEach var="j" begin="10" end="59">
                                        <option value="${j}">${j}</option>
                                    </c:forEach>

                                </select>
                            </td>
                            <td class="reportType">Report Type</td>
                            <td class="reportType">
                                <select name="reportType">
                                    <option value="">Please!!! Select Report Type</option>
                                    <option value="NO_DCP_Report">Ordinary Report</option>
                                    <option value="DCP_Report">DCP Report</option>
                                    <option value="All">All</option>
                                </select>
                            </td>
                            <td class="deposit">Type</td>
                            <td class="deposit">
                                <select name="transactionType">
                                    <option value="deposit">Deposit</option>
                                    <option value="billPayment">Payment</option>
                                    <option value="withdrawal">Withdrawal</option>
                                </select>
                            </td>
                            <td class="billCreator">Bill Creator :</td>
                            <td class="billCreator"><openmrs_tag:userField formFieldName="billCreator"
                                                                           initialValue="${billCreator}"/></td>
                        </tr>
                        <tr>
                            <td>End Date:</td>
                            <td><input type="text" size="11"
                                       value="${endDate}" name="endDate" onclick="showCalendar(this)"/></td>
                            <td class="timelabel">hh</td>
                            <td>
                                <select name="endHour" class="time">
                                    <option value="00">00</option>
                                    <c:forEach var="counter" begin="1" end="9">
                                        <option value="0${counter}">0${counter}</option>
                                    </c:forEach>

                                    <c:forEach var="j" begin="10" end="23">
                                        <option value="${j}">${j}</option>
                                    </c:forEach>
                                </select>
                            </td>

                            <td class="timelabel">mm</td>
                            <td>
                                <select name="endMinute" class="time">
                                    <option value="00">00</option>
                                    <c:forEach var="i" begin="1" end="9">
                                        <option value="0${i}">0${i}</option>
                                    </c:forEach>

                                    <c:forEach var="j" begin="10" end="59">
                                        <option value="${j}">${j}</option>
                                    </c:forEach>

                                </select>
                            </td>

                            <td class="billStatus">Bill Status</td>
                            <td class="billStatus">
                                <select name="billStatus">
                                    <option value="">---</option>
                                    <option value="FULLY PAID" ${billStatus== 'FULLY PAID' ? 'selected' : ''}>FULLY
                                        PAID
                                    </option>
                                    <option value="UNPAID" ${billStatus== 'UNPAID' ? 'selected' : ''}>UNPAID</option>
                                    <option value="PARTLY PAID" ${billStatus== 'PARTLY PAID' ? 'selected' : ''}>PARTLY
                                        PAID
                                    </option>
                                </select>
                            </td>

                            <td class="collector">Collector :</td>
                            <td class="collector"><openmrs_tag:userField formFieldName="cashCollector"
                                                                         initialValue="${cashCollector}"
                                                                         roles="Cashier;Chief Cashier"/></td>

                            <td class="paymentType">Type</td>
                            <td class="paymentType">
                                <select name="paymentType">
                                    <option value="">All</option>
                                    <!-- <option value="cashPayment">Cash Payment</option>
                                        <option value="depositPayment">Deposit Payment</option> -->
                                </select>
                            </td>
                        </tr>

                        <tr class="insurances">
                            <td class="insurances">Insurance:</td>
                            <td class="insurances">
                                <select name="insuranceId" class="insurance">
                                    <option value="">--select--</option>
                                    <c:forEach items="${insurances }" var="insurance">
                                        <option value="${insurance.insuranceId }">${insurance.name }</option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>
                        <tr class="services">
                            <td class="services">Service :</td>
                            <td class="services">
                                <select name="departmentId">
                                    <option value="">--select--</option>
                                    <c:forEach items="${departments }" var="department">
                                        <option value="${department.departmentId }">${department.name }</option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>
                        <tr class="thirdParties">
                            <td class="thirdParties">Third Party :</td>
                            <td class="thirdParties">
                                <select name="thirdPartyId" class="thirdParty">
                                    <option value="">--select--</option>
                                    <c:forEach items="${thirdParties }" var="thirdParty">
                                        <option value="${thirdParty.thirdPartyId }">${thirdParty.name }</option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>

                    </table>
                </td>

                <!-- </tr>

			<tr class="insurances">
			<td>Insurance : </td>
				<td>
				  <select name="insuranceId" class="insurance">
				    <option value="">--select--</option>
				    <c:forEach items="${insurances }" var="insurance">
				    <option value="${insurance.insuranceId }">${insurance.name }</option>
				    </c:forEach>
				  </select>
				</td>

				<td class="services">Service : </td>
				<td class="services">
				  <select name="departmentId">
				    <option value="">--select--</option>
				    <c:forEach items="${departments }" var="department">
				    <option value="${department.departmentId }">${department.name }</option>
				    </c:forEach>
				  </select>
				</td>
			</tr>

			<tr class="thirdParties">
			<td>Third Party : </td>
				<td>
				  <select name="thirdPartyId" class="thirdParty">
				    <option value="">--select--</option>
				    <c:forEach items="${thirdParties }" var="thirdParty">
				    <option value="${thirdParty.thirdPartyId }">${thirdParty.name }</option>
				    </c:forEach>
				  </select>
				</td> -->
            </tr>
        </table>
        <input type="submit" value="Search" id="submitId"/>
        <c:if test="${totalRecords > 1}">
            <div class="pagination">

                <div class="dataTables_info">Showing 1 to ${pageSize} of ${totalRecords} records</div>
                <div class="dataTables_paginate fg-buttonset fg-buttonset-multi paging_full_numbers" style="width: 75%">
                    <!-- Show Previous button if not on the first page -->
                    <c:if test="${currentPage > 1}">
                        <a href="javascript:void(0)" onclick="goToPage(${currentPage - 1})">Previous</a>
                    </c:if>

                    <!-- Calculate page window -->
                    <c:set var="pageWindowSize" value="10"/>
                    <c:set var="startPage"
                           value="${(currentPage - pageWindowSize / 2) > 0 ? (currentPage - pageWindowSize / 2) : 1}"/>
                    <c:set var="endPage"
                           value="${(startPage + pageWindowSize - 1) < totalPages ? (startPage + pageWindowSize - 1) : totalPages}"/>

                    <!-- Always show the first page -->
                    <c:if test="${startPage > 1}">
                        <a href="javascript:void(0)" onclick="goToPage(1)">1</a>
                        <!-- Show ellipsis if needed -->
                        <c:if test="${startPage > 2}">
                            <span>...</span>
                        </c:if>
                    </c:if>

                    <!-- Display the page window -->
                    <c:forEach var="i" begin="${startPage}" end="${endPage}">
                        <c:if test="${i == currentPage}">
                            <strong>${i}</strong>
                        </c:if>
                        <c:if test="${i != currentPage}">
                            <a href="javascript:void(0)" onclick="goToPage(${i})">${i}</a>
                        </c:if>
                    </c:forEach>

                    <!-- Always show the last page -->
                    <c:if test="${endPage < totalPages}">
                        <c:if test="${endPage < totalPages - 1}">
                            <span>...</span>
                        </c:if>
                        <a href="javascript:void(0)" onclick="goToPage(${totalPages})">${totalPages}</a>
                    </c:if>

                    <!-- Show Next button if not on the last page -->
                    <c:if test="${currentPage < totalPages}">
                        <a href="javascript:void(0)" onclick="goToPage(${currentPage + 1})">Next</a>
                    </c:if>
                </div>
            </div>
        </c:if>
    </form>
</div>