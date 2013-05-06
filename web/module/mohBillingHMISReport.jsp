<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<b class="boxHeader"> Select period</b>
<div class="box">

	<form action="hmisReport.form" method="post" name="">

		<table>
			<tr>
				<td width="10%">When?</td>
				<td>
					<table>
						<tr>
							<td>On Or After <input type="text" size="11"
								value="${startDate}" name="startDate"
								onclick="showCalendar(this)" /></td>
						</tr>
						<tr>
							<td>On Or Before <input type="text" size="11"
								value="${endDate}" name="endDate" onclick="showCalendar(this)" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>

		<input type="submit" value="Search" />

	</form>
</div>
<br />
<b class="boxHeader"> HMIS indicators</b>
<div class="box" id="billing_hmis">
	<table width="1120" border="1" cellspacing="0">
		<tr class="tableHeadBlue">
			<td colspan="4"><strong class="head">XX. Finances </strong></td>
		</tr>
		<tr class="tbleHead">
			<td colspan="2">A) Receipts(from all sources including
				Insurance)</td>
			<td colspan="2">B) Expenditures</td>
		</tr>
		<tr>
			<td width="293">Description</td>
			<td width="102">Total Amount</td>
			<td width="459">Description</td>
			<td width="248" class="tbleHead">Total Amount</td>
		</tr>
		<tr>
			<td>1. Preventive care</td>
			<td>&nbsp;</td>
			<td>1. Purchase of medecines,medical materials</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>2. Curative care(including hospitalization)</td>
			<td>&nbsp;</td>
			<td>2. Salaries, socila security,professional taxes,personnel
				payments</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>3. Deliveries</td>
			<td>&nbsp;</td>
			<td>3. Employee bonuses</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>4. Laboratory</td>
			<td>&nbsp;</td>
			<td>5. Office supplies/printed materials/medical records</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>5. Sale of medecines/supplies</td>
			<td>&nbsp;</td>
			<td>6. Maintenance and repair of medical equipment</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>6. Minor surgery</td>
			<td>&nbsp;</td>
			<td>7. Maintenance and repair of non-medical equipment</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>7. Issue of Medical-Legal Documents</td>
			<td>&nbsp;</td>
			<td>9. Maintenance and repair of transport</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>8. Sale of patient records/forms</td>
			<td>&nbsp;</td>
			<td>10. Maintenance/cleaning supplies</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>9. Transpot of patients</td>
			<td>&nbsp;</td>
			<td>11. Fuel and motor oil</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>10. Performance Based Financing</td>
			<td>&nbsp;</td>
			<td>12. Water and Electricity</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td height="23">11. Other State Subsidies</td>
			<td>&nbsp;</td>
			<td><p>13. Communication(Telephone,Internet...)</p></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>12. Contributions from other donors</td>
			<td>&nbsp;</td>
			<td>14. Training</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>13. Bank interest</td>
			<td>&nbsp;</td>
			<td>15. Costs associated with indigents</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>14. Other receipts</td>
			<td>&nbsp;</td>
			<td>16. Purchase medical equipment</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>Total Receipts(A)</td>
			<td>&nbsp;</td>
			<td>17. Purchase non-medical equipment</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>18. Purchase transport</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>19. Other expenses</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>Total Expenses (B)</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	<br />
	<table width="1123" border="1" cellspacing="0">
		<tr class="tbleHead">
			<td width="214">C. Mutuelle receipts</td>
			<td width="105">Total Amount</td>
			<td width="531">D. Other health insurance
				receipts(RAMA/MMI/FARG/Private insurers)</td>
			<td width="255">Total Amount</td>
		</tr>
		<tr>
			<td>14.1 co-payments</td>
			<td>&nbsp;</td>
			<td>15.1 Co-payments</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>14.2 Payment for care</td>
			<td>&nbsp;</td>
			<td>15.2 Payment for care</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>14.3 Payment for medication</td>
			<td>&nbsp;</td>
			<td>15.3 Payment for medication</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	&nbsp;
	<table width="1123" height="211" border="1" cellspacing="0">
		<tr class="tbleHead">
			<td colspan="2">E) Credits</td>
			<td colspan="2">Debts</td>
		</tr>
		<tr>
			<td width="320">Description</td>
			<td width="148">Amount</td>
			<td width="279">Description</td>
			<td width="358">Amount</td>
		</tr>
		<tr>
			<td>1. Credits at the beginning of the month (e)</td>
			<td>&nbsp;</td>
			<td>1 Debts at the beginning of the month (i)</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>2. (+) Additional credits during the month (f)</td>
			<td>&nbsp;</td>
			<td>2. (+) Total debts this month(j)</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>3. (-) Reimbursements during the month (g)</td>
			<td>&nbsp;</td>
			<td>3. (-) Reimbursements this month (k)</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>4. Total credits at the end of the month (H)=(e+f)-(g)</td>
			<td>&nbsp;</td>
			<td>4. Debt at the end of the month (L) = (i+j)-(k)</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td height="73" colspan="4">Total credits: all parties who owe
				the FOSA money, goods(e.g medecines) or services (ex. consultation)
				provided <br> Total debts: all parties whose the FOSA owes
				money, goods (e.g medicine) or services (ex.consultation) provided
			</td>
		</tr>
	</table>
	&nbsp;
	<table width="1123" height="236" border="1" cellspacing="0">
		<tr class="tbleHead">
			<td colspan="5">G) Financial Statement</td>
		</tr>

		<tr>
			<td width="327">Description</td>
			<td width="98">Amount</td>
			<td width="127">&nbsp;</td>
			<td width="305">Description</td>
			<td width="244">Amount</td>
		</tr>
		<tr>
			<td>1. General bank account (m)</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>6. Total available at the beginning of the month (r)</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>2. (+) Pharmacy bank account (n)</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>7. (+) balance of receipts and expenses (s)=A-B</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>3. (+) General cash on hand (o)</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>4. (+) Pharmacy cash on hand (p)</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td height="23">5. Total available at the end of month (Q) =
				m+n+o+p</td>
			<td height="23">&nbsp;</td>
			<td height="23">Q=T</td>
			<td height="23">8. Total available at the end of the month (T) =
				r+s</td>
			<td height="23">&nbsp;</td>
		</tr>
	</table>
	<table width="1123" border="0" cellspacing="0">
		<tr class="bdyColor">
			<th width="174" class="bdyColor" scope="col">H) Receip in hand</th>
			<th colspan="4" class="bdyColor" scope="col">I) Pending Receipts</th>
			<th width="283" class="bdyColor" scope="col">J) Total pending
				receipts</th>
		</tr>
		<tr class="bdyColor">
			<td class="bdyColor">1. From the population (C)</td>
			<td width="88" class="bdyColor">2.Indigents (u)</td>
			<td width="171" class="bdyColor">3. Other non-paying client* (v)</td>
			<td width="139" class="bdyColor">4. Credits for goods and
				services during the months** (w)</td>
			<td width="252" class="bdyColor">5. Total receipts non received
				(X)= u+v+w</td>
			<td class="bdyColor">(Y) = (C) + (X)</td>
		</tr>
		<tr>
			<td height="80" colspan="4">&nbsp;</td>
			<td class="bdyColor"><p>I) Ration of pending
					receipts/receipts in hand</p>
				<p>(Z) = (X)*100/(Y)</p></td>
			<td class="bdyColor">&nbsp;</td>
		</tr>
	</table>

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>