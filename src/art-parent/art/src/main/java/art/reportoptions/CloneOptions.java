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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Options for clone reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloneOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean useParentParameters = true;
	private boolean useParentRules = true;

	/**
	 * @return the useParentRules
	 */
	public boolean isUseParentRules() {
		return useParentRules;
	}

	/**
	 * @param useParentRules the useParentRules to set
	 */
	public void setUseParentRules(boolean useParentRules) {
		this.useParentRules = useParentRules;
	}

	/**
	 * @return the useParentParameters
	 */
	public boolean isUseParentParameters() {
		return useParentParameters;
	}

	/**
	 * @param useParentParameters the useParentParameters to set
	 */
	public void setUseParentParameters(boolean useParentParameters) {
		this.useParentParameters = useParentParameters;
	}

}
