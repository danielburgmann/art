/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.migration;

import art.datasource.Datasource;
import art.enums.MigrationFileFormat;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.Serializable;

/**
 * Represents an export records operation
 *
 * @author Timothy Anyona
 */
public class ExportRecords implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String EMBEDDED_SCHEDULES_FILENAME = "art-export-Schedules.csv";
	public static final String EMBEDDED_HOLIDAYS_FILENAME = "art-export-Holidays.csv";
	public static final String EMBEDDED_USERS_FILENAME = "art-export-Users.csv";
	public static final String EMBEDDED_USERGROUPS_FILENAME = "art-export-UserGroups.csv";
	public static final String EMBEDDED_CSV_REPORTS_FILENAME = "art-export-Reports.csv";
	public static final String EMBEDDED_JSON_REPORTS_FILENAME = "art-export-Reports.json";
	public static final String EMBEDDED_REPORTGROUPS_FILENAME = "art-export-ReportGroups.csv";
	public static final String EMBEDDED_REPORTPARAMETERS_FILENAME = "art-export-ReportParameters.csv";
	public static final String EMBEDDED_USERRULEVALUES_FILENAME = "art-export-UserRuleValues.csv";
	public static final String EMBEDDED_USERGROUPRULEVALUES_FILENAME = "art-export-UserGroupRuleValues.csv";
	public static final String EMBEDDED_REPORTRULES_FILENAME = "art-export-ReportRules.csv";
	public static final String EMBEDDED_USERREPORTRIGHTS_FILENAME = "art-export-UserReportRights.csv";
	public static final String EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME = "art-export_UserGroupReportRights.csv";
	public static final String EMBEDDED_DRILLDOWNS_FILENAME = "art-export-Drilldowns.csv";
	public static final String EMBEDDED_DRILLDOWNREPORTPARAMETERS_FILENAME = "art-export-DrilldownReportParameters.csv";
	public static final String EMBEDDED_ROLES_FILENAME = "art-export-Roles.csv";
	public static final String EMBEDDED_PERMISSIONS_FILENAME = "art-export-Permissions.csv";
	public static final String EMBEDDED_CSV_PARAMETERS_FILENAME = "art-export-Parameters.csv";
	public static final String EMBEDDED_JSON_PARAMETERS_FILENAME = "art-export-Parameters.json";
	public static final String EMBEDDED_CSV_ENCRYPTORS_FILENAME = "art-export-Encryptors.csv";
	public static final String EMBEDDED_JSON_ENCRYPTORS_FILENAME = "art-export-Encryptors.json";

	private MigrationRecordType recordType;
	private String ids;
	private MigrationLocation location = MigrationLocation.File;
	private Datasource datasource;
	private MigrationFileFormat fileFormat = MigrationFileFormat.json;
	private boolean overwrite;

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @return the fileFormat
	 */
	public MigrationFileFormat getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat the fileFormat to set
	 */
	public void setFileFormat(MigrationFileFormat fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * @return the recordType
	 */
	public MigrationRecordType getRecordType() {
		return recordType;
	}

	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(MigrationRecordType recordType) {
		this.recordType = recordType;
	}

	/**
	 * @return the ids
	 */
	public String getIds() {
		return ids;
	}

	/**
	 * @param ids the ids to set
	 */
	public void setIds(String ids) {
		this.ids = ids;
	}

	/**
	 * @return the location
	 */
	public MigrationLocation getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(MigrationLocation location) {
		this.location = location;
	}

	/**
	 * @return the datasource
	 */
	public Datasource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	/**
	 * Returns a csv schema to be used for importing/exporting records to csv
	 *
	 * @param csvMapper the csv mapper object
	 * @param type the class for which the schema is to be built
	 * @return the csv schema to be used for importing/exporting records
	 */
	public static CsvSchema getCsvSchema(CsvMapper csvMapper, Class<?> type) {
		//https://stackoverflow.com/questions/15144641/what-is-the-difference-between-class-clazz-and-class-clazz-in-java/15144835
		//https://github.com/FasterXML/jackson-dataformat-csv/issues/112
		CsvSchema schema = csvMapper.schemaFor(type).withHeader().withNullValue("NULL");
		return schema;
	}
}
