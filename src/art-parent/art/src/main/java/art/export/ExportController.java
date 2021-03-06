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
package art.export;

import art.report.ReportService;
import art.servlets.Config;
import art.user.User;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller to serve report or job file output
 *
 * @author Timothy Anyona
 */
@Controller
public class ExportController {
	//https://stackoverflow.com/questions/5673260/downloading-a-file-from-spring-controllers

	private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

	//https://stackoverflow.com/questions/16332092/spring-mvc-pathvariable-with-dot-is-getting-truncated
	//https://stackoverflow.com/questions/27419743/spring-path-variable-truncate-after-dot-annotation
	@GetMapping("/export/jobs/{filename:.+}")
	public void serveJobFile(@PathVariable("filename") String filename,
			HttpServletRequest request, HttpServletResponse response) {

		logger.debug("Entering serveJobFile: filename='{}'", filename);

		String jobsExportPath = Config.getJobsExportPath();
		String fullFilename = jobsExportPath + filename;
		serveFile(fullFilename, request, response);
	}

	@GetMapping("/export/jobLogs/{filename:.+}")
	public void serveJobLogFile(@PathVariable("filename") String filename,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {

		logger.debug("Entering serveJobLogFile: filename='{}'", filename);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (!sessionUser.hasPermission("configure_jobs")) {
				request.getRequestDispatcher("/accessDenied").forward(request, response);
				return;
			}
		} catch (ServletException | IOException ex) {
			logger.error("Error", ex);
			return;
		}

		String jobLogsPath = Config.getJobLogsPath();
		String fullFilename = jobLogsPath + filename;
		serveFile(fullFilename, request, response);
	}

	@GetMapping("/export/records/{filename:.+}")
	public void serveRecordsExportFile(@PathVariable("filename") String filename,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {

		logger.debug("Entering serveRecordsExportFile: filename='{}'", filename);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (!sessionUser.hasPermission("migrate_records")) {
				request.getRequestDispatcher("/accessDenied").forward(request, response);
				return;
			}
		} catch (ServletException | IOException ex) {
			logger.error("Error", ex);
			return;
		}

		String recordsExportPath = Config.getRecordsExportPath();
		String fullFilename = recordsExportPath + filename;
		serveFile(fullFilename, request, response);
	}

	@GetMapping("/export/reports/{filename:.+}")
	public void serveReportFile(@PathVariable("filename") String filename,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {

		logger.debug("Entering serveReportFile: filename='{}'", filename);

		String reportsExportPath = Config.getReportsExportPath();
		String fullFilename = reportsExportPath + filename;

		if (Config.getCustomSettings().isCheckExportFileAccess()) {
			String baseName = FilenameUtils.getBaseName(filename);
			String uptoReportId = StringUtils.substringBeforeLast(baseName, "-");
			String reportIdString = StringUtils.substringAfterLast(uptoReportId, "-");
			int reportId = Integer.parseInt(reportIdString);

			ReportService reportService = new ReportService();
			User sessionUser = (User) session.getAttribute("sessionUser");
			try {
				if (!reportService.canUserRunReport(sessionUser, reportId)) {
					request.getRequestDispatcher("/accessDenied").forward(request, response);
					return;
				}
			} catch (SQLException | ServletException | IOException ex) {
				logger.error("Error", ex);
				return;
			}
		}

		serveFile(fullFilename, request, response);
	}

	/**
	 * Serves a file to the http response
	 *
	 * @param fullFilename the full path to the file to serve
	 * @param request the http request
	 * @param response the http response
	 */
	private void serveFile(String fullFilename, HttpServletRequest request,
			HttpServletResponse response) {

		logger.debug("Entering serveFile: fullFilename='{}'", fullFilename);

		File file = new File(fullFilename);

		if (file.exists()) {
			ExportHelper exportHelper = new ExportHelper();
			exportHelper.serveFile(file, response);
		} else {
			request.setAttribute("message", "reports.message.fileNotFound");
			try {
				request.getRequestDispatcher("/accessDenied").forward(request, response);
			} catch (ServletException | IOException ex) {
				logger.error("Error", ex);
			}
		}
	}

}
