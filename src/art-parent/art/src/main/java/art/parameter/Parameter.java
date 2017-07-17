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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.Report;
import art.utils.ArtUtils;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represent a parameter
 *
 * @author Timothy Anyona
 */
public class Parameter implements Serializable {

	private static final long serialVersionUID = 1L;
	private int parameterId;
	private String name;
	private ParameterType parameterType;
	private String label;
	private String helpText;
	private ParameterDataType dataType;
	private String defaultValue;
	private boolean hidden;
	private boolean useLov;
	private int lovReportId;
	private boolean useRulesInLov;
	private int drilldownColumnIndex;
	private boolean useDirectSubstitution;
	private Date creationDate;
	private Date updateDate;
	private String description;
	private String createdBy;
	private String updatedBy;
	private Report defaultValueReport;
	private boolean shared;
	private String options;
	private String dateFormat;
	private ParameterOptions parameterOptions;
	private String placeholderText;

	/**
	 * @return the placeholderText
	 */
	public String getPlaceholderText() {
		return placeholderText;
	}

	/**
	 * @param placeholderText the placeholderText to set
	 */
	public void setPlaceholderText(String placeholderText) {
		this.placeholderText = placeholderText;
	}

	/**
	 * @return the parameterOptions
	 */
	public ParameterOptions getParameterOptions() {
		return parameterOptions;
	}

	/**
	 * @param parameterOptions the parameterOptions to set
	 */
	public void setParameterOptions(ParameterOptions parameterOptions) {
		this.parameterOptions = parameterOptions;
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
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the shared
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * @param shared the shared to set
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	/**
	 * @return the defaultValueReport
	 */
	public Report getDefaultValueReport() {
		return defaultValueReport;
	}

	/**
	 * @param defaultValueReport the defaultValueReport to set
	 */
	public void setDefaultValueReport(Report defaultValueReport) {
		this.defaultValueReport = defaultValueReport;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the parameterId
	 */
	public int getParameterId() {
		return parameterId;
	}

	/**
	 * @param parameterId the parameterId to set
	 */
	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameterType
	 */
	public ParameterType getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the helpText
	 */
	public String getHelpText() {
		return helpText;
	}

	/**
	 * @param helpText the helpText to set
	 */
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	/**
	 * @return the dataType
	 */
	public ParameterDataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(ParameterDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the useLov
	 */
	public boolean isUseLov() {
		return useLov;
	}

	/**
	 * @param useLov the useLov to set
	 */
	public void setUseLov(boolean useLov) {
		this.useLov = useLov;
	}

	/**
	 * @return the lovReportId
	 */
	public int getLovReportId() {
		return lovReportId;
	}

	/**
	 * @param lovReportId the lovReportId to set
	 */
	public void setLovReportId(int lovReportId) {
		this.lovReportId = lovReportId;
	}

	/**
	 * @return the useRulesInLov
	 */
	public boolean isUseRulesInLov() {
		return useRulesInLov;
	}

	/**
	 * @param useRulesInLov the useRulesInLov to set
	 */
	public void setUseRulesInLov(boolean useRulesInLov) {
		this.useRulesInLov = useRulesInLov;
	}

	/**
	 * @return the drilldownColumnIndex
	 */
	public int getDrilldownColumnIndex() {
		return drilldownColumnIndex;
	}

	/**
	 * @param drilldownColumnIndex the drilldownColumnIndex to set
	 */
	public void setDrilldownColumnIndex(int drilldownColumnIndex) {
		this.drilldownColumnIndex = drilldownColumnIndex;
	}

	/**
	 * @return the useDirectSubstitution
	 */
	public boolean isUseDirectSubstitution() {
		return useDirectSubstitution;
	}

	/**
	 * @param useDirectSubstitution the useDirectSubstitution to set
	 */
	public void setUseDirectSubstitution(boolean useDirectSubstitution) {
		this.useDirectSubstitution = useDirectSubstitution;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + this.parameterId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Parameter other = (Parameter) obj;
		if (this.parameterId != other.parameterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Parameter{" + "parameterId=" + parameterId + '}';
	}

	/**
	 * Returns the html element name that should be used for this parameter
	 *
	 * @return the html element name that should be used for this parameter
	 */
	public String getHtmlElementName() {
		return ArtUtils.PARAM_PREFIX + name;
	}

	/**
	 * Returns the default value string to be used in html elements. Null is
	 * returned as an empty string.
	 *
	 * @return the default value string to be used in html elements
	 */
	public String getHtmlDefaultValue() {
		String value = defaultValue;

		if (defaultValue == null) {
			value = "";
		}

		return getHtmlValue(value);
	}

	/**
	 * Returns the string that should be used in html elements
	 *
	 * @param value the original value
	 * @return the string that should be used in html elements
	 */
	public String getHtmlValue(Object value) {
		switch (dataType) {
			case Date:
			case DateTime:
				//convert date to string that will be recognised by parameter processor class
				return getDateString(value);
			default:
				return String.valueOf(value);
		}
	}

	/**
	 * Returns a date string for a given date object, formatted according to the
	 * parameter's date format setting
	 *
	 * @param value the date value
	 * @return the formatted date string
	 */
	public String getDateString(Object value) {
		switch (dataType) {
			case Date:
			case DateTime:
				if (StringUtils.isBlank(dateFormat)) {
					if (dataType == ParameterDataType.Date) {
						return ArtUtils.isoDateFormatter.format(value);
					} else {
						return ArtUtils.isoDateTimeFormatter.format(value);
					}
				} else {
					SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
					return dateFormatter.format(value);
				}
			default:
				throw new IllegalArgumentException("Unexpected parameter data type: " + dataType);
		}
	}

	/**
	 * Returns the label to use for this parameter, given a particular locale,
	 * taking into consideration the i18n options defined for the parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized label
	 * @throws java.io.IOException
	 */
	public String getLocalizedLabelWithLocale(Locale locale) throws IOException {
		//can't use method overloading as this functionality is used from jsp with el, and el can't reliably call overloaded methods
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		if (locale == null) {
			return label;
		} else {
			return getLocalizedLabel(locale.toString());
		}
	}

	/**
	 * Returns the label to use for this parameter, given a particular locale,
	 * taking into consideration the i18n options defined for the parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the localized label
	 * @throws java.io.IOException
	 */
	public String getLocalizedLabel(String localeString) throws IOException {
		String localizedLabel = null;

		if (parameterOptions != null && StringUtils.isNotBlank(localeString)) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nLabelOptions = i18nOptions.getLabel();
				localizedLabel = getLocalizedValue(localeString, i18nLabelOptions);
			}
		}

		if (localizedLabel == null) {
			localizedLabel = label;
		}

		return localizedLabel;
	}
	
	/**
	 * Returns the help text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized help text
	 * @throws java.io.IOException
	 */
	public String getLocalizedHelpTextWithLocale(Locale locale) throws IOException {
		//can't use method overloading as this functionality is used from jsp with el, and el can't reliably call overloaded methods
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		if (locale == null) {
			return helpText;
		} else {
			return getLocalizedHelpText(locale.toString());
		}
	}

	/**
	 * Returns the help text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the localized help text
	 * @throws java.io.IOException
	 */
	public String getLocalizedHelpText(String localeString) throws IOException {
		String localizedHelpText = null;

		if (parameterOptions != null && StringUtils.isNotBlank(localeString)) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nHelpTextOptions = i18nOptions.getHelpText();
				localizedHelpText = getLocalizedValue(localeString, i18nHelpTextOptions);
			}
		}

		if (localizedHelpText == null) {
			localizedHelpText = helpText;
		}

		return localizedHelpText;
	}
	
	/**
	 * Returns the default value to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized default value
	 * @throws java.io.IOException
	 */
	public String getLocalizedDefaultValueWithLocale(Locale locale) throws IOException {
		//can't use method overloading as this functionality is used from jsp with el, and el can't reliably call overloaded methods
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		if (locale == null) {
			return defaultValue;
		} else {
			return getLocalizedDefaultValue(locale.toString());
		}
	}

	/**
	 * Returns the default value to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the localized default value
	 * @throws java.io.IOException
	 */
	public String getLocalizedDefaultValue(String localeString) throws IOException {
		String localizedDefaultValue = null;

		if (parameterOptions != null && StringUtils.isNotBlank(localeString)) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nDefaultValueOptions = i18nOptions.getDefaultValue();
				localizedDefaultValue = getLocalizedValue(localeString, i18nDefaultValueOptions);
			}
		}

		if (localizedDefaultValue == null) {
			localizedDefaultValue = defaultValue;
		}

		return localizedDefaultValue;
	}
	
	/**
	 * Returns the placeholder text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized placeholder text
	 * @throws java.io.IOException
	 */
	public String getLocalizedPlaceholderTextWithLocale(Locale locale) throws IOException {
		//can't use method overloading as this functionality is used from jsp with el, and el can't reliably call overloaded methods
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		if (locale == null) {
			return placeholderText;
		} else {
			return getLocalizedPlaceholderText(locale.toString());
		}
	}

	/**
	 * Returns the placeholder text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the localized placeholder text
	 * @throws java.io.IOException
	 */
	public String getLocalizedPlaceholderText(String localeString) throws IOException {
		String localizedPlaceholderText = null;

		if (parameterOptions != null && StringUtils.isNotBlank(localeString)) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nPlaceholderTextOptions = i18nOptions.getPlaceholderText();
				localizedPlaceholderText = getLocalizedValue(localeString, i18nPlaceholderTextOptions);
			}
		}

		if (localizedPlaceholderText == null) {
			localizedPlaceholderText = placeholderText;
		}

		return localizedPlaceholderText;
	}

	/**
	 * Returns an i18n value to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @param i18nValueOptions the i18n definition of locales and values
	 * @return the localized value to use, or null if a localization is not
	 * found
	 */
	private String getLocalizedValue(String localeString,
			List<Map<String, String>> i18nValueOptions) {

		String localizedValue = null;

		if (CollectionUtils.isNotEmpty(i18nValueOptions)) {
			boolean valueFound = false;
			for (Map<String, String> i18nValueOption : i18nValueOptions) {
				//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
				// Get the first entry that the iterator returns
				Entry<String, String> entry = i18nValueOption.entrySet().iterator().next();
				String localeSetting = entry.getKey();
				String localeValue = entry.getValue();
				String[] locales = StringUtils.split(localeSetting, ",");
				for (String locale : locales) {
					if (StringUtils.equalsIgnoreCase(locale.trim(), localeString)) {
						localizedValue = localeValue;
						valueFound = true;
						break;
					}
				}

				if (valueFound) {
					break;
				}
			}
		}

		return localizedValue;
	}
	
	/**
	 * Returns the html type attribute to use for text input
	 * 
	 * @return 
	 */
	public String getHtmlTextInputType(){
		switch(dataType){
			case Integer:
				return "number";
			default:
				return "text";
		}
	}
}
