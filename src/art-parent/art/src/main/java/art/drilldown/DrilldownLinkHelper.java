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
package art.drilldown;

import art.drilldown.Drilldown;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.reportparameter.ReportParameter;
import art.utils.ArtUtils;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Provides methods for generating drilldown links
 *
 * @author Timothy Anyona
 */
public class DrilldownLinkHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Drilldown drilldown;
	private final List<Parameter> drilldownParams;
	private final Set<String> drilldownParamNames;
	private final List<ReportParameter> reportParamsList;
	private final Locale locale;

	/**
	 * Constructs a new drilldown link helper
	 *
	 * @param drilldown the drilldown to use
	 * @param reportParamsList the report parameters, may be null
	 * @param locale the locale to use
	 * @throws SQLException
	 */
	public DrilldownLinkHelper(Drilldown drilldown, List<ReportParameter> reportParamsList,
			 Locale locale) throws SQLException {
		
		Objects.requireNonNull(drilldown, "drilldown must not be null");

		this.drilldown = drilldown;
		this.reportParamsList = reportParamsList;
		this.locale=locale;

		int drilldownReportId = drilldown.getDrilldownReport().getReportId();
		ParameterService parameterService = new ParameterService();
		drilldownParams = parameterService.getDrilldownParameters(drilldownReportId);

		//store parameter names so that parent parameters with the same name
		//as in the drilldown report are omitted
		//use hashset for fast searching using contains
		//https://stackoverflow.com/questions/3307549/fastest-way-to-check-if-a-liststring-contains-a-unique-string
		drilldownParamNames = new HashSet<>();
		for (Parameter drilldownParam : drilldownParams) {
			String paramName = drilldownParam.getName();
			drilldownParamNames.add(paramName);
		}
	}

	/**
	 * Adds the drilldown base url
	 *
	 * @param sb drilldown link string builder
	 */
	private void addDrilldownBaseUrl(StringBuilder sb) {
		if (drilldown != null) {
			int drilldownReportId = drilldown.getDrilldownReport().getReportId();
			String drilldownReportFormat = drilldown.getReportFormat();
			if (drilldownReportFormat == null || drilldownReportFormat.equalsIgnoreCase("all")) {
				drilldownReportFormat = "default";
			}
			sb.append("runReport?reportId=").append(drilldownReportId)
					.append("&reportFormat=").append(drilldownReportFormat)
					.append("&allowSelectParameters=true")
					.append("&startSelectParametersHidden=true");
		}
	}

	/**
	 * Adds a parameter to the drilldown url
	 *
	 * @param paramName the parameter name
	 * @param paramValue the parameter value
	 * @param sb the drilldown link string builder
	 */
	private void addUrlParameter(String paramName, String paramValue, StringBuilder sb) {
		if (paramName == null || paramValue == null || sb == null) {
			return;
		}

		try {
			String encodedParamName = URLEncoder.encode(paramName, "UTF-8");
			String encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
			sb.append("&").append(ArtUtils.PARAM_PREFIX).append(encodedParamName).append("=").append(encodedParamValue);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds parameters from the parent report
	 *
	 * @param sb the drilldown link string builder
	 */
	private void addParentParameters(StringBuilder sb) {
		if (reportParamsList == null) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			String[] paramValues = reportParam.getPassedParameterValues();

			//add parameter only if one with a similar name doesn't already
			//exist in the drill down parameters
			if (drilldownParamNames == null || !drilldownParamNames.contains(paramName)) {
				if (paramValues != null) {
					for (String paramValue : paramValues) {
						addUrlParameter(paramName, paramValue, sb);
					}
				}
			}
		}
	}

	/**
	 * Returns the drilldown link to use
	 *
	 * @param paramValues the parameters to include
	 * @return the drilldown link to use
	 */
	public String getDrilldownLink(Object... paramValues) {
		StringBuilder sb = new StringBuilder(200);

		//add base url
		addDrilldownBaseUrl(sb);

		//add drilldown parameters
		if (drilldownParams != null) {
			for (Parameter drilldownParam : drilldownParams) {
				int drilldownColumnIndex = drilldownParam.getDrilldownColumnIndex();
				String paramName = drilldownParam.getName();
				Object paramValueObject = paramValues[drilldownColumnIndex - 1];
				String paramValueString = drilldownParam.getHtmlValue(paramValueObject, locale);
				addUrlParameter(paramName, paramValueString, sb);
			}
		}

		//add parameters from parent report
		addParentParameters(sb);

		String drilldownUrl = sb.toString();

		return drilldownUrl;
	}
}