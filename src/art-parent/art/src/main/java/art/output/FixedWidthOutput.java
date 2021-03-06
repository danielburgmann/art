/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.ReportFormat;
import art.report.Report;
import art.reportoptions.FixedWidthOptions;
import art.runreport.GroovyDataDetails;
import art.runreport.RunReportHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.common.processor.ObjectRowWriterProcessor;
import com.univocity.parsers.fixed.FieldAlignment;
import com.univocity.parsers.fixed.FixedWidthFields;
import com.univocity.parsers.fixed.FixedWidthFormat;
import com.univocity.parsers.fixed.FixedWidthRoutines;
import com.univocity.parsers.fixed.FixedWidthWriter;
import com.univocity.parsers.fixed.FixedWidthWriterSettings;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates fixed width output
 *
 * @author Timothy Anyona
 */
public class FixedWidthOutput {

	//https://stackoverflow.com/questions/8669967/java-fixed-width-file-format-read-write-library
	//https://github.com/uniVocity/univocity-parsers/blob/master/src/test/java/com/univocity/parsers/examples/FixedWidthWriterExamples.java
	private static final Logger logger = LoggerFactory.getLogger(FixedWidthOutput.class);

	private ResultSet resultSet;
	private Object data;

	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Generates fixed width output for data in the given resultset
	 *
	 * @param writer the writer to output to. If html report format is required,
	 * a writer must be supplied
	 * @param report the report object for the report being run
	 * @param reportFormat the report format to use
	 * @param fullOutputFileName the output file name to use
	 * @param locale the locale that determines date format output
	 * @throws java.lang.Exception
	 */
	public void generateOutput(PrintWriter writer, Report report,
			ReportFormat reportFormat, String fullOutputFileName, Locale locale)
			throws Exception {

		logger.debug("Entering generateOutput: report={}, reportFormat={},"
				+ " fullOutputFileName='{}'", report, reportFormat, fullOutputFileName);

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			throw new IllegalArgumentException("Options not specified");
		}

		ObjectMapper mapper = new ObjectMapper();
		FixedWidthOptions fixedWidthOptions = mapper.readValue(options, FixedWidthOptions.class);

		FixedWidthFields fields;

		List<Integer> fieldLengths = fixedWidthOptions.getFieldLengths();
		List<Map<String, Integer>> fieldLengthsByName = fixedWidthOptions.getFieldLengthsByName();

		ResultSetMetaData rsmd = null;

		List<List<Object>> listData = new ArrayList<>();
		List<String> columnLabels = null;
		if (resultSet != null) {
			rsmd = resultSet.getMetaData();
		} else if (data != null) {
			GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data, report);
			columnLabels = dataDetails.getColumnLabels();
			List<Map<String, ?>> mapListData = RunReportHelper.getMapListData(data);
			for (Map<String, ?> row : mapListData) {
				List<Object> rowData = new ArrayList<>();
				for (Object value : row.values()) {
					rowData.add(value);
				}
				listData.add(rowData);
			}
		}

		if (CollectionUtils.isNotEmpty(fieldLengths)) {
			//https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
			int[] fieldLengthsArray = ArrayUtils.toPrimitive(fixedWidthOptions.getFieldLengths().toArray(new Integer[0]));

			List<String> columnNames = new ArrayList<>();
			if (rsmd != null) {
				for (int i = 1; i <= fieldLengthsArray.length; i++) {
					columnNames.add(rsmd.getColumnLabel(i));
				}
			} else if (columnLabels != null) {
				for (int i = 1; i <= fieldLengthsArray.length; i++) {
					columnNames.add(columnLabels.get(i - 1));
				}
			}

			String[] headers = columnNames.toArray(new String[0]);

			fields = new FixedWidthFields(headers, fieldLengthsArray);
		} else if (CollectionUtils.isNotEmpty(fieldLengthsByName)) {
			fields = new FixedWidthFields();
			//addField() will just add fields in the order added, which may not be the order in the resultset
			if (rsmd != null) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columnName = rsmd.getColumnLabel(i);
					for (Map<String, Integer> fieldLengthDefinition : fieldLengthsByName) {
						//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
						// Get the first entry that the iterator returns
						Entry<String, Integer> entry = fieldLengthDefinition.entrySet().iterator().next();
						String fieldName = entry.getKey();
						Integer fieldLength = entry.getValue();
						if (StringUtils.equalsIgnoreCase(columnName, fieldName)) {
							fields.addField(fieldName, fieldLength);
							break;
						}
					}
				}
			} else if (columnLabels != null) {
				for (String columnName : columnLabels) {
					for (Map<String, Integer> fieldLengthDefinition : fieldLengthsByName) {
						//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
						// Get the first entry that the iterator returns
						Entry<String, Integer> entry = fieldLengthDefinition.entrySet().iterator().next();
						String fieldName = entry.getKey();
						Integer fieldLength = entry.getValue();
						if (StringUtils.equalsIgnoreCase(columnName, fieldName)) {
							fields.addField(fieldName, fieldLength);
							break;
						}
					}
				}
			}
		} else {
			throw new RuntimeException("fieldLengths or fieldLengthsByName not defined");
		}

		List<Map<String, List<String>>> fieldAlignmentByName = fixedWidthOptions.getFieldAlignmentByName();
		if (CollectionUtils.isNotEmpty(fieldAlignmentByName)) {
			for (Map<String, List<String>> alignmentDefinition : fieldAlignmentByName) {
				// Get the first entry that the iterator returns
				Entry<String, List<String>> entry = alignmentDefinition.entrySet().iterator().next();
				String alignment = entry.getKey();
				List<String> fieldNames = entry.getValue();
				String[] fieldNamesArray = fieldNames.toArray(new String[0]);
				if (StringUtils.equalsIgnoreCase(alignment, "left")) {
					fields.setAlignment(FieldAlignment.LEFT, fieldNamesArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "right")) {
					fields.setAlignment(FieldAlignment.RIGHT, fieldNamesArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "center")) {
					fields.setAlignment(FieldAlignment.CENTER, fieldNamesArray);
				} else {
					throw new IllegalArgumentException("Invalid field alignment: " + alignment);
				}
			}
		}

		List<Map<String, List<Integer>>> fieldAlignmentByPosition = fixedWidthOptions.getFieldAlignmentByPosition();
		if (CollectionUtils.isNotEmpty(fieldAlignmentByPosition)) {
			for (Map<String, List<Integer>> alignmentDefinition : fieldAlignmentByPosition) {
				// Get the first entry that the iterator returns
				Entry<String, List<Integer>> entry = alignmentDefinition.entrySet().iterator().next();
				String alignment = entry.getKey();
				List<Integer> fieldPositions = entry.getValue();
				int[] fieldPositionsArray = ArrayUtils.toPrimitive(fieldPositions.toArray(new Integer[0]));
				if (StringUtils.equalsIgnoreCase(alignment, "left")) {
					fields.setAlignment(FieldAlignment.LEFT, fieldPositionsArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "right")) {
					fields.setAlignment(FieldAlignment.RIGHT, fieldPositionsArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "center")) {
					fields.setAlignment(FieldAlignment.CENTER, fieldPositionsArray);
				} else {
					throw new IllegalArgumentException("Invalid field alignment: " + alignment);
				}
			}
		}

		List<Map<Character, List<String>>> fieldPaddingByName = fixedWidthOptions.getFieldPaddingByName();
		if (CollectionUtils.isNotEmpty(fieldPaddingByName)) {
			for (Map<Character, List<String>> paddingDefinition : fieldPaddingByName) {
				// Get the first entry that the iterator returns
				Entry<Character, List<String>> entry = paddingDefinition.entrySet().iterator().next();
				Character padding = entry.getKey();
				List<String> fieldNames = entry.getValue();
				String[] fieldNamesArray = fieldNames.toArray(new String[0]);
				fields.setPadding(padding, fieldNamesArray);
			}
		}

		List<Map<Character, List<Integer>>> fieldPaddingByPosition = fixedWidthOptions.getFieldPaddingByPosition();
		if (CollectionUtils.isNotEmpty(fieldPaddingByPosition)) {
			for (Map<Character, List<Integer>> paddingDefinition : fieldPaddingByPosition) {
				// Get the first entry that the iterator returns
				Entry<Character, List<Integer>> entry = paddingDefinition.entrySet().iterator().next();
				Character padding = entry.getKey();
				List<Integer> fieldPositions = entry.getValue();
				int[] fieldPositionsArray = ArrayUtils.toPrimitive(fieldPositions.toArray(new Integer[0]));
				fields.setPadding(padding, fieldPositionsArray);
			}
		}

		FixedWidthWriterSettings writerSettings = new FixedWidthWriterSettings(fields);

		ObjectRowWriterProcessor processor = new ObjectRowWriterProcessor();
		fixedWidthOptions.initializeProcessor(processor, locale);

		writerSettings.setRowWriterProcessor(processor);
		writerSettings.setHeaderWritingEnabled(fixedWidthOptions.isIncludeHeaders());
		writerSettings.setUseDefaultPaddingForHeaders(fixedWidthOptions.isUseDefaultPaddingForHeaders());

		FixedWidthFormat fixedWidthFormat = writerSettings.getFormat();
		fixedWidthFormat.setPadding(fixedWidthOptions.getPadding());

		String defaultAlignmentForHeaders = fixedWidthOptions.getDefaultAlignmentForHeaders();
		if (StringUtils.isNotBlank(defaultAlignmentForHeaders)) {
			if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "left")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.LEFT);
			} else if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "right")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.RIGHT);
			} else if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "center")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.CENTER);
			} else {
				throw new IllegalArgumentException("Invalid defaultAlignmentForHeaders: " + defaultAlignmentForHeaders);
			}
		}

		FixedWidthRoutines routines = new FixedWidthRoutines(writerSettings);
		routines.setKeepResourcesOpen(true);

		if (reportFormat.isHtml()) {
			writer.println("<pre>");
			if (resultSet != null) {
				routines.write(resultSet, writer);
			} else {
				FixedWidthWriter fixedWidthWriter = new FixedWidthWriter(writer, writerSettings);
				for (List<Object> row : listData) {
					fixedWidthWriter.processRecord(row.toArray(new Object[0]));
				}
			}
			writer.println("</pre>");
		} else {
			try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
				if (reportFormat == ReportFormat.txt) {
					if (resultSet != null) {
						routines.write(resultSet, fout, "UTF-8");
					} else {
						FixedWidthWriter fixedWidthWriter = new FixedWidthWriter(fout, "UTF-8", writerSettings);
						for (List<Object> row : listData) {
							fixedWidthWriter.processRecord(row.toArray(new Object[0]));
						}
						fixedWidthWriter.close();
					}
				} else if (reportFormat == ReportFormat.txtZip) {
					String filename = FilenameUtils.getBaseName(fullOutputFileName);
					ZipEntry ze = new ZipEntry(filename + ".txt");
					try (ZipOutputStream zout = new ZipOutputStream(fout)) {
						zout.putNextEntry(ze);
						if (resultSet != null) {
							routines.write(resultSet, zout, "UTF-8");
						} else {
							FixedWidthWriter fixedWidthWriter = new FixedWidthWriter(zout, "UTF-8", writerSettings);
							for (List<Object> row : listData) {
								fixedWidthWriter.processRecord(row.toArray(new Object[0]));
							}
							fixedWidthWriter.close();
						}
					}
				}
			}
		}
	}

}
