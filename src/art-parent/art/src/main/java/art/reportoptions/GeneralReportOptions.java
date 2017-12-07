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

/**
 * Represents report options that can be used in a wide variety of report types
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralReportOptions {

	private boolean usesGroovy;
	private Reporti18nOptions i18n;

	/**
	 * @return the i18n
	 */
	public Reporti18nOptions getI18n() {
		return i18n;
	}

	/**
	 * @param i18n the i18n to set
	 */
	public void setI18n(Reporti18nOptions i18n) {
		this.i18n = i18n;
	}

	/**
	 * @return the usesGroovy
	 */
	public boolean isUsesGroovy() {
		return usesGroovy;
	}

	/**
	 * @param usesGroovy the usesGroovy to set
	 */
	public void setUsesGroovy(boolean usesGroovy) {
		this.usesGroovy = usesGroovy;
	}
}
