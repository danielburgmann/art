/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.reportoptions;

import java.util.List;
import java.util.Map;

/**
 * Represents options for fixed width reports
 *
 * @author Timothy Anyona
 */
public class FixedWidthOptions {

	private String dateFormat;
	private String dateTimeFormat;
	private List<Integer> fieldLengths;
	private boolean includeHeaders = true;
	private char padding = ' ';
	private boolean useDefaultPaddingForHeaders = true;
	private String defaultAlignmentForHeaders;
	private List<Map<String, Integer>> fieldLengthsByName;
	private List<Map<String,List<String>>> fieldAlignmentByName;
	private List<Map<String,List<Integer>>> fieldAlignmentByPosition;
	private List<Map<Character,List<String>>> fieldPaddingByName;
	private List<Map<Character,List<Integer>>> fieldPaddingByPosition;
	private String numberFormat;
	private List<Map<String,List<String>>> fieldNumberFormats;
	private List<Map<String,List<String>>> fieldIntegerFormats;
	private List<Map<String,List<String>>> fieldLongFormats;
	private List<Map<String,List<String>>> fieldDoubleFormats;
	private List<Map<String,List<String>>> fieldDateFormats;

	/**
	 * @return the padding
	 */
	public char getPadding() {
		return padding;
	}

	/**
	 * @param padding the padding to set
	 */
	public void setPadding(char padding) {
		this.padding = padding;
	}

	/**
	 * @return the fieldLengths
	 */
	public List<Integer> getFieldLengths() {
		return fieldLengths;
	}

	/**
	 * @param fieldLengths the fieldLengths to set
	 */
	public void setFieldLengths(List<Integer> fieldLengths) {
		this.fieldLengths = fieldLengths;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the dateTimeFormat
	 */
	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @param dateTimeFormat the dateTimeFormat to set
	 */
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	/**
	 * @return the includeHeaders
	 */
	public boolean isIncludeHeaders() {
		return includeHeaders;
	}

	/**
	 * @param includeHeaders the includeHeaders to set
	 */
	public void setIncludeHeaders(boolean includeHeaders) {
		this.includeHeaders = includeHeaders;
	}

	/**
	 * @return the useDefaultPaddingForHeaders
	 */
	public boolean isUseDefaultPaddingForHeaders() {
		return useDefaultPaddingForHeaders;
	}

	/**
	 * @param useDefaultPaddingForHeaders the useDefaultPaddingForHeaders to set
	 */
	public void setUseDefaultPaddingForHeaders(boolean useDefaultPaddingForHeaders) {
		this.useDefaultPaddingForHeaders = useDefaultPaddingForHeaders;
	}

	/**
	 * @return the defaultAlignmentForHeaders
	 */
	public String getDefaultAlignmentForHeaders() {
		return defaultAlignmentForHeaders;
	}

	/**
	 * @param defaultAlignmentForHeaders the defaultAlignmentForHeaders to set
	 */
	public void setDefaultAlignmentForHeaders(String defaultAlignmentForHeaders) {
		this.defaultAlignmentForHeaders = defaultAlignmentForHeaders;
	}

	/**
	 * @return the fieldLengthsByName
	 */
	public List<Map<String, Integer>> getFieldLengthsByName() {
		return fieldLengthsByName;
	}

	/**
	 * @param fieldLengthsByName the fieldLengthsByName to set
	 */
	public void setFieldLengthsByName(List<Map<String, Integer>> fieldLengthsByName) {
		this.fieldLengthsByName = fieldLengthsByName;
	}

	/**
	 * @return the fieldAlignmentByName
	 */
	public List<Map<String,List<String>>> getFieldAlignmentByName() {
		return fieldAlignmentByName;
	}

	/**
	 * @param fieldAlignmentByName the fieldAlignmentByName to set
	 */
	public void setFieldAlignmentByName(List<Map<String,List<String>>> fieldAlignmentByName) {
		this.fieldAlignmentByName = fieldAlignmentByName;
	}

	/**
	 * @return the fieldAlignmentByPosition
	 */
	public List<Map<String,List<Integer>>> getFieldAlignmentByPosition() {
		return fieldAlignmentByPosition;
	}

	/**
	 * @param fieldAlignmentByPosition the fieldAlignmentByPosition to set
	 */
	public void setFieldAlignmentByPosition(List<Map<String,List<Integer>>> fieldAlignmentByPosition) {
		this.fieldAlignmentByPosition = fieldAlignmentByPosition;
	}

	/**
	 * @return the fieldPaddingByName
	 */
	public List<Map<Character,List<String>>> getFieldPaddingByName() {
		return fieldPaddingByName;
	}

	/**
	 * @param fieldPaddingByName the fieldPaddingByName to set
	 */
	public void setFieldPaddingByName(List<Map<Character,List<String>>> fieldPaddingByName) {
		this.fieldPaddingByName = fieldPaddingByName;
	}

	/**
	 * @return the fieldPaddingByPosition
	 */
	public List<Map<Character,List<Integer>>> getFieldPaddingByPosition() {
		return fieldPaddingByPosition;
	}

	/**
	 * @param fieldPaddingByPosition the fieldPaddingByPosition to set
	 */
	public void setFieldPaddingByPosition(List<Map<Character,List<Integer>>> fieldPaddingByPosition) {
		this.fieldPaddingByPosition = fieldPaddingByPosition;
	}

	/**
	 * @return the numberFormat
	 */
	public String getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat the numberFormat to set
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * @return the fieldIntegerFormats
	 */
	public List<Map<String,List<String>>> getFieldIntegerFormats() {
		return fieldIntegerFormats;
	}

	/**
	 * @param fieldIntegerFormats the fieldIntegerFormats to set
	 */
	public void setFieldIntegerFormats(List<Map<String,List<String>>> fieldIntegerFormats) {
		this.fieldIntegerFormats = fieldIntegerFormats;
	}

	/**
	 * @return the fieldDateFormats
	 */
	public List<Map<String,List<String>>> getFieldDateFormats() {
		return fieldDateFormats;
	}

	/**
	 * @param fieldDateFormats the fieldDateFormats to set
	 */
	public void setFieldDateFormats(List<Map<String,List<String>>> fieldDateFormats) {
		this.fieldDateFormats = fieldDateFormats;
	}

	/**
	 * @return the fieldLongFormats
	 */
	public List<Map<String,List<String>>> getFieldLongFormats() {
		return fieldLongFormats;
	}

	/**
	 * @param fieldLongFormats the fieldLongFormats to set
	 */
	public void setFieldLongFormats(List<Map<String,List<String>>> fieldLongFormats) {
		this.fieldLongFormats = fieldLongFormats;
	}

	/**
	 * @return the fieldDoubleFormats
	 */
	public List<Map<String,List<String>>> getFieldDoubleFormats() {
		return fieldDoubleFormats;
	}

	/**
	 * @param fieldDoubleFormats the fieldDoubleFormats to set
	 */
	public void setFieldDoubleFormats(List<Map<String,List<String>>> fieldDoubleFormats) {
		this.fieldDoubleFormats = fieldDoubleFormats;
	}

	/**
	 * @return the fieldNumberFormats
	 */
	public List<Map<String,List<String>>> getFieldNumberFormats() {
		return fieldNumberFormats;
	}

	/**
	 * @param fieldNumberFormats the fieldNumberFormats to set
	 */
	public void setFieldNumberFormats(List<Map<String,List<String>>> fieldNumberFormats) {
		this.fieldNumberFormats = fieldNumberFormats;
	}
}
