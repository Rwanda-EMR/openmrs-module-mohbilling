<table width="100%">
	<tr>
		<td><b><spring:message code='@MODULE_ID@.export.report.creator'/></b></td>
		<td colspan="3"><input type="text" size="60" readonly="readonly" name="creator" value="${creator.personName}"/></td>
	</tr>
	<tr>
		<td><b><spring:message code='@MODULE_ID@.export.report.name'/></b></td>
		<td colspan="3"><input type="text" size="60" name="reportName" value="${reportName}"/></td>
	</tr>
	<tr>
		<td><b><spring:message code='@MODULE_ID@.export.report.date'/></b></td>
		<td><input type="text" name="dateOfExport" size="11" value="${today}" readonly="readonly"/></td>
		<td><div style="text-align: right;">
			<select name="dateFormat">
				<option value="yyyy-MM-dd"><spring:message code='@MODULE_ID@.export.report.date.format.yyyyMMdd'/></option>
				<option value="yyyy-MMM-dd"><spring:message code='@MODULE_ID@.export.report.date.format.yyyyMMMdd'/></option>
				<option value="dd-MM-yyyy"><spring:message code='@MODULE_ID@.export.report.date.format.ddMMyyyy'/></option>
				<option value="dd-MMM-yyyy"><spring:message code='@MODULE_ID@.export.report.date.format.ddMMMyyyy'/></option>
			</select>
			</div>
		</td>
		<td><b><spring:message code='@MODULE_ID@.export.report.date.format'/></b></td>
	</tr>
	<tr>
		<td><b><spring:message code='@MODULE_ID@.export.report.file.format'/></b></td>
		<td><select name="fileFormat">
				<option value="csv"><spring:message code='@MODULE_ID@.export.report.file.format.csv'/></option>
				<option value="xls"><spring:message code='@MODULE_ID@.export.report.file.format.excel'/></option>
				<option value="pdf"><spring:message code='@MODULE_ID@.export.report.file.format.pdf'/></option>
			</select>
		</td>
		<td><div style="text-align: right;"><select name="reportLanguage">
				<option value="en"><spring:message code='@MODULE_ID@.export.report.language.en'/></option>
				<option value="fr"><spring:message code='@MODULE_ID@.export.report.language.fr'/></option>
			</select></div>
		</td>
		<td><b><spring:message code='@MODULE_ID@.export.report.language'/></b></td>
	</tr>
	<tr>
		<td></td>
		<td colspan="3"><br/><input type="submit" value="<spring:message code='@MODULE_ID@.export.report.create'/>"/></td>
	</tr>
</table>