/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class to represent custom settings that can't be set from the user interface.
 * Mainly to enhance security.
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSettings {

	private boolean showErrors = true; //whether exception details are shown in the user interface
	private boolean enableDirectParameterSubstitution; //whether direct parameter values in report sql is enabled - instead of using preparedstatements
	private String exportDirectory; //custom path for export files i.e. for files generated by reports and jobs
	private String workDirectory; //custom work directory for art files e.g. settings, templates

	/**
	 * Get the value of workDirectory
	 *
	 * @return the value of workDirectory
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * Set the value of workDirectory
	 *
	 * @param workDirectory new value of workDirectory
	 */
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the showErrors
	 */
	public boolean isShowErrors() {
		return showErrors;
	}

	/**
	 * @param showErrors the showErrors to set
	 */
	public void setShowErrors(boolean showErrors) {
		this.showErrors = showErrors;
	}

	/**
	 * @return the enableDirectParameterSubstitution
	 */
	public boolean isEnableDirectParameterSubstitution() {
		return enableDirectParameterSubstitution;
	}

	/**
	 * @param enableDirectParameterSubstitution the
	 * enableDirectParameterSubstitution to set
	 */
	public void setEnableDirectParameterSubstitution(boolean enableDirectParameterSubstitution) {
		this.enableDirectParameterSubstitution = enableDirectParameterSubstitution;
	}

	/**
	 * @return the exportDirectory
	 */
	public String getExportDirectory() {
		return exportDirectory;
	}

	/**
	 * @param exportDirectory the exportDirectory to set
	 */
	public void setExportDirectory(String exportDirectory) {
		this.exportDirectory = exportDirectory;
	}

}
