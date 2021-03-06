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
package art.report;

/**
 * Represents response for a jquery file upload event
 * 
 * @author Timothy Anyona
 */
public class FileUploadResponse {
	//https://github.com/blueimp/jQuery-File-Upload/wiki/Setup#using-jquery-file-upload-ui-version-with-a-custom-server-side-upload-handler
	
	private String name;
	private long size;
	private String error;
	private String deleteType = "POST";
	private String deleteUrl;

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
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return the deleteType
	 */
	public String getDeleteType() {
		return deleteType;
	}

	/**
	 * @param deleteType the deleteType to set
	 */
	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}

	/**
	 * @return the deleteUrl
	 */
	public String getDeleteUrl() {
		return deleteUrl;
	}

	/**
	 * @param deleteUrl the deleteUrl to set
	 */
	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}
	
}
