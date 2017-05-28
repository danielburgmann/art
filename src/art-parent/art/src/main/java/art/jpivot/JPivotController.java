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
package art.jpivot;

import art.datasource.Datasource;
import art.enums.ReportType;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.ruleValue.RuleValueService;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.user.User;
import art.usergroup.UserGroup;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.olap.model.OlapModelDecorator;
import net.sf.jpivotart.jpivot.olap.query.MdxOlapModel;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.tags.OlapModelProxy;
import net.sf.wcfart.wcf.form.FormComponent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for displaying jpivot reports
 *
 * @author Timothy Anyona
 */
@Controller
public class JPivotController {

	private static final Logger logger = LoggerFactory.getLogger(JPivotController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportRuleService reportRuleService;

	@Autowired
	private RuleValueService ruleValueService;

	@RequestMapping(value = "/showJPivot", method = {RequestMethod.GET, RequestMethod.POST})
	public String showJPivot(HttpServletRequest request, Model model, HttpSession session,
			Locale locale) {

		logger.debug("Entering showJPivot");

		try {
			int reportId = 0;
			String reportIdParameter = request.getParameter("reportId");
			if (reportIdParameter == null) {
				//not passed when using olap navigator
				Integer sessionReportId = (Integer) session.getAttribute("pivotReportId");
				logger.debug("sessionReportId={}", sessionReportId);
				if (sessionReportId != null) {
					reportId = sessionReportId;
				}
			} else {
				//save to session in case olap navigator is used
				reportId = Integer.parseInt(reportIdParameter);
				session.setAttribute("pivotReportId", reportId);

				logger.debug("reportId={}", reportId);
			}

			Report report = reportService.getReport(reportId);

			String errorPage = "reportError";

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			String reportName = report.getName();
			model.addAttribute("reportName", reportName);

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			User sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.isAdminUser()) {
				if (!report.isActive()) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}
			}

			if (report.getReportType() == ReportType.JPivotMondrian) {
				String templateFileName = report.getTemplate();
				String templatesPath = Config.getTemplatesPath();
				String fullTemplateFileName = templatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					model.addAttribute("message", "reports.message.templateFileNotSpecified");
					return errorPage;
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					model.addAttribute("message", "reports.message.templateFileNotFound");
					return errorPage;
				}
			}

			model.addAttribute("localeString", locale.toString());

			prepareVariables(request, session, report, model, sessionUser);

		} catch (SQLException | RuntimeException | ParseException | MalformedURLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "showJPivot";
	}

	/**
	 * Prepares model and session attributes in readiness for pivot table
	 * display
	 *
	 * @param request
	 * @param session
	 * @param report
	 * @param model
	 * @param sessionUser
	 * @throws NumberFormatException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws MalformedURLException
	 */
	private void prepareVariables(HttpServletRequest request, HttpSession session,
			Report report, Model model, User sessionUser)
			throws NumberFormatException, SQLException, ParseException, MalformedURLException {

		int reportId = report.getReportId();
		model.addAttribute("reportId", reportId);

		String jpivotQueryId = "query" + reportId;
		model.addAttribute("jpivotQueryId", jpivotQueryId);

		if (request.getParameter("action") == null && request.getParameter("null") == null) {
			//first time we are displaying the pivot table.
			//parameter ?null=null&... used to display the page when settings are changed from the olap navigator toolbar button

			String template = report.getTemplate();
			String schemaFile = Config.getRelativeDefaultTemplatesPath() + template;
			model.addAttribute("schemaFile", schemaFile);

			ReportType reportType = report.getReportType();
			model.addAttribute("reportType", reportType);

			//put title in session. may be lost if olap navigator option on jpivot toolbar is used
			String title = report.getName();
			session.setAttribute("pivotTitle" + reportId, title);

			Datasource datasource = report.getDatasource();
			if (reportType == ReportType.JPivotMondrian) {
				String databaseUrl = datasource.getUrl().trim();
				String databaseUser = datasource.getUsername().trim();
				String databasePassword = datasource.getPassword();
				String databaseDriver = datasource.getDriver().trim();

				model.addAttribute("databaseUrl", databaseUrl);
				model.addAttribute("databaseUser", databaseUser);
				model.addAttribute("databasePassword", databasePassword);
				model.addAttribute("databaseDriver", databaseDriver);

				String roles = getRolesString(reportId, sessionUser);
				model.addAttribute("roles", roles);
			} else {
				//construct xmla url to incoporate username and password if present
				String xmlaUrl = datasource.getUrl();
				String xmlaUsername = datasource.getUsername();
				String xmlaPassword = datasource.getPassword();

				URL url = new URL(xmlaUrl);
				if (StringUtils.length(xmlaUsername) > 0) {
					xmlaUrl = url.getProtocol() + "://" + xmlaUsername;
					if (StringUtils.length(xmlaPassword) > 0) {
						xmlaUrl += ":" + xmlaPassword;
					}
					int port = url.getPort();
					if (port == -1) {
						//no port specified
						xmlaUrl += "@" + url.getHost() + url.getPath();
					} else {
						xmlaUrl += "@" + url.getHost() + ":" + port + url.getPath();
					}
				}
				model.addAttribute("xmlaUrl", xmlaUrl);

				String xmlaDatasource = null;
				if (reportType == ReportType.JPivotMondrianXmla) {
					//prepend provider if only datasource name provided
					xmlaDatasource = report.getXmlaDatasource();
					if (!StringUtils.startsWithIgnoreCase(xmlaDatasource, "provider=mondrian")) {
						xmlaDatasource = "Provider=mondrian;DataSource=" + xmlaDatasource; //datasource name in datasources.xml file must be exactly the same
					}
				} else if (reportType == ReportType.JPivotSqlServerXmla) {
					xmlaDatasource = "Provider=MSOLAP";

				}
				model.addAttribute("xmlaDatasource", xmlaDatasource);

				String xmlaCatalog = report.getXmlaCatalog();
				model.addAttribute("xmlaCatalog", xmlaCatalog);
			}

			ReportRunner reportRunner = null;
			try {
				reportRunner = new ReportRunner();
				reportRunner.setUser(sessionUser);
				reportRunner.setReport(report);

				//prepare report parameters
				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();

				reportRunner.setReportParamsMap(reportParamsMap);

				reportRunner.execute();

				String query = reportRunner.getQuerySql();
				model.addAttribute("query", query);
			} finally {
				if (reportRunner != null) {
					reportRunner.close();
				}
			}

			//check if this is the only user who has access. if so, he can overwrite the pivot table view with a different view
			boolean exclusiveAccess = reportService.hasExclusiveAccess(sessionUser, report);

			//save status in the session. will be lost as navigation is done on the pivot table
			session.setAttribute("pivotExclusiveAccess" + reportId, exclusiveAccess);
		}

		//get title from session
		String title = (String) session.getAttribute("pivotTitle" + reportId);
		model.addAttribute("title", title);

		//get exclusive access status from the session
		Boolean exclusiveAccess = (Boolean) session.getAttribute("pivotExclusiveAccess" + reportId);
		model.addAttribute("exclusiveAccess", exclusiveAccess);

		//set identifiers for jpivot objects
		String tableId = "table" + reportId;
		String mdxEditId = "mdxedit" + reportId;
		String printId = "print" + reportId;
		String printFormId = "printform" + reportId;
		String navigatorId = "navi" + reportId;
		String sortFormId = "sortform" + reportId;
		String chartId = "chart" + reportId;
		String chartFormId = "chartform" + reportId;
		String toolbarId = "toolbar" + reportId;

		String queryDrillThroughTable = jpivotQueryId + ".drillthroughtable";

		String modelQueryId = "#{" + jpivotQueryId + "}";
		String modelTableId = "#{" + tableId + "}";
		String modelPrintId = "#{" + printId + "}";
		String modelChartId = "#{" + chartId + "}";

		String mdxEditVisible = "#{" + mdxEditId + ".visible}";
		String navigatorVisible = "#{" + navigatorId + ".visible}";
		String sortFormVisible = "#{" + sortFormId + ".visible}";
		String tableLevelStyle = "#{" + tableId + ".extensions.axisStyle.levelStyle}";
		String tableHideSpans = "#{" + tableId + ".extensions.axisStyle.hideSpans}";
		String tableShowProperties = "#{" + tableId + ".rowAxisBuilder.axisConfig.propertyConfig.showProperties}";
		String tableNonEmptyButtonPressed = "#{" + tableId + ".extensions.nonEmpty.buttonPressed}";
		String tableSwapAxesButtonPressed = "#{" + tableId + ".extensions.swapAxes.buttonPressed}";
		String tableDrillMemberEnabled = "#{" + tableId + ".extensions.drillMember.enabled}";
		String tableDrillPositionEnabled = "#{" + tableId + ".extensions.drillPosition.enabled}";
		String tableDrillReplaceEnabled = "#{" + tableId + ".extensions.drillReplace.enabled}";
		String tableDrillThroughEnabled = "#{" + tableId + ".extensions.drillThrough.enabled}";
		String chartVisible = "#{" + chartId + ".visible}";
		String chartFormVisible = "#{" + chartFormId + ".visible}";
		String printFormVisible = "#{" + printFormId + ".visible}";

		String printExcel = request.getContextPath() + "/Print?cube=" + reportId + "&type=0";
		String printPdf = request.getContextPath() + "/Print?cube=" + reportId + "&type=1";

		model.addAttribute("tableId", tableId);
		model.addAttribute("mdxEditId", mdxEditId);
		model.addAttribute("printId", printId);
		model.addAttribute("printFormId", printFormId);
		model.addAttribute("navigatorId", navigatorId);
		model.addAttribute("sortFormId", sortFormId);
		model.addAttribute("chartId", chartId);
		model.addAttribute("chartFormId", chartFormId);
		model.addAttribute("toolbarId", toolbarId);
		model.addAttribute("queryDrillThroughTable", queryDrillThroughTable);
		model.addAttribute("modelQueryId", modelQueryId);
		model.addAttribute("modelTableId", modelTableId);
		model.addAttribute("modelPrintId", modelPrintId);
		model.addAttribute("modelChartId", modelChartId);
		model.addAttribute("mdxEditVisible", mdxEditVisible);
		model.addAttribute("navigatorVisible", navigatorVisible);
		model.addAttribute("sortFormVisible", sortFormVisible);
		model.addAttribute("tableLevelStyle", tableLevelStyle);
		model.addAttribute("tableHideSpans", tableHideSpans);
		model.addAttribute("tableShowProperties", tableShowProperties);
		model.addAttribute("tableNonEmptyButtonPressed", tableNonEmptyButtonPressed);
		model.addAttribute("tableSwapAxesButtonPressed", tableSwapAxesButtonPressed);
		model.addAttribute("tableDrillMemberEnabled", tableDrillMemberEnabled);
		model.addAttribute("tableDrillPositionEnabled", tableDrillPositionEnabled);
		model.addAttribute("tableDrillReplaceEnabled", tableDrillReplaceEnabled);
		model.addAttribute("tableDrillThroughEnabled", tableDrillThroughEnabled);
		model.addAttribute("chartVisible", chartVisible);
		model.addAttribute("chartFormVisible", chartFormVisible);
		model.addAttribute("printFormVisible", printFormVisible);
		model.addAttribute("printExcel", printExcel);
		model.addAttribute("printPdf", printPdf);

		//get the current mdx
		String currentMdx = "";
		TableComponent table = (TableComponent) session.getAttribute(tableId);
		if (table != null) {
			OlapModel olapModel = table.getOlapModel();
			while (olapModel != null) {
				if (olapModel instanceof OlapModelProxy) {
					OlapModelProxy proxy = (OlapModelProxy) olapModel;
					olapModel = proxy.getDelegate();
				}
				if (olapModel instanceof OlapModelDecorator) {
					OlapModelDecorator decorator = (OlapModelDecorator) olapModel;
					olapModel = decorator.getDelegate();
				}
				if (olapModel instanceof MdxOlapModel) {
					MdxOlapModel mdxOlapModel = (MdxOlapModel) olapModel;
					currentMdx = mdxOlapModel.getCurrentMdx();
					olapModel = null;
				}
			}
		}

		//save current mdx in the session
		session.setAttribute("mdx" + reportId, currentMdx);

		//get object with olap query and result
		OlapModel _olapModel = (OlapModel) session.getAttribute(jpivotQueryId);

		String overflowResult = null;
		if (_olapModel != null) {
			try {
				_olapModel.getResult();
				if (_olapModel.getResult().isOverflowOccured()) {
					overflowResult = "Resultset overflow occurred";
				}
			} catch (Throwable t) {
				logger.error("Error", t);
				overflowResult = "Error occurred while getting resultset";
			}
		}
		model.addAttribute("overflowResult", overflowResult);

		FormComponent _mdxEdit = (FormComponent) session.getAttribute(mdxEditId);
		if (_mdxEdit != null && _mdxEdit.isVisible()) {
			model.addAttribute("mdxEditIsVisible", true);
		}
	}

	/**
	 * Returns the roles string to use for jpivot mondrian roles configuration
	 *
	 * @param reportId the id of the current report
	 * @param sessionUser the current session user
	 * @return the roles string to use for jpivot mondrian roles configuration
	 * @throws SQLException
	 */
	private String getRolesString(int reportId, User sessionUser) throws SQLException {
		//get roles to be applied. use rule values are roles
		List<String> roles = new ArrayList<>();
		List<ReportRule> reportRules = reportRuleService.getReportRules(reportId);

		for (ReportRule reportRule : reportRules) {
			int userId = sessionUser.getUserId();
			int ruleId = reportRule.getRule().getRuleId();
			List<String> userRuleValues = ruleValueService.getUserRuleValues(userId, ruleId);
			roles.addAll(userRuleValues);

			for (UserGroup userGroup : sessionUser.getUserGroups()) {
				List<String> userGroupRuleValues = ruleValueService.getUserGroupRuleValues(userGroup.getUserGroupId(), ruleId);
				roles.addAll(userGroupRuleValues);
			}
		}

		//https://stackoverflow.com/questions/3317691/replace-elements-in-a-list-with-another
		Collections.replaceAll(roles, ",", ",,");

		//remove duplicates
		//https://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
		Set<String> distinctRoles = new LinkedHashSet<>(roles);

		String rolesString = StringUtils.join(distinctRoles, ",");

		return rolesString;
	}

	@RequestMapping(value = "/jpivotError", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotError(HttpServletRequest request, Model model) {
		logger.debug("Entering jpivotError");

		String errorDetails = "";

		Throwable e = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
		while (e != null) {
			errorDetails = errorDetails + e.toString() + "<br><br>";

			Throwable prev = e;
			e = e.getCause();
			if (e == prev) {
				break;
			}
		}

		model.addAttribute("errorDetails", errorDetails);

		return "jpivotError";
	}

	@RequestMapping(value = "/jpivotBusy", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotBusy() {
		logger.debug("Entering jpivotBusy");

		return "jpivotBusy";
	}

	@RequestMapping(value = "/saveJPivot", method = RequestMethod.POST)
	public String saveJPivot(HttpServletRequest request,
			HttpSession session, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveJPivot");

		try {
			boolean overwriting;
			if (request.getParameter("overwrite") != null) {
				overwriting = true;
			} else {
				overwriting = false;
			}

			boolean deleting;
			if (request.getParameter("delete") != null && !overwriting) {
				deleting = true;
			} else {
				deleting = false;
			}

			int reportId = Integer.parseInt(request.getParameter("pivotReportId"));
			String mdx = (String) session.getAttribute("mdx" + reportId);

			//check if any modification made
			if ((mdx == null || mdx.length() == 0) && !deleting) {
				redirectAttributes.addFlashAttribute("message", "jpivot.message.nothingToSave");
				return "redirect:/reportError";
			}

			Report report = reportService.getReport(reportId);

			if (report == null) {
				redirectAttributes.addFlashAttribute("message", "reports.message.reportNotFound");
				return "redirect:/reportError";
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			String queryDescription = request.getParameter("newPivotDescription");

			if (overwriting) {
				//overwrite query source with current mdx
				//query details loaded. update query
				report.setReportSource(mdx);
				if (StringUtils.length(queryDescription) > 0) {
					//update description
					report.setDescription(queryDescription);
				}
				reportService.updateReport(report, sessionUser);
				redirectAttributes.addFlashAttribute("message", "jpivot.message.reportSaved");
				return "redirect:/success";
			} else if (deleting) {
				//delete query
				reportService.deleteReport(reportId);
				redirectAttributes.addFlashAttribute("message", "jpivot.message.reportDeleted");
				return "redirect:/success";
			} else {
				//create new query based on current query
				Report newReport = new Report();

				newReport.setReportType(report.getReportType());
				newReport.setShortDescription("");
				newReport.setContactPerson(sessionUser.getUsername());
				newReport.setUsesRules(report.isUsesRules());
				newReport.setTemplate(report.getTemplate());
				newReport.setActive(report.isActive());
				newReport.setHidden(report.isHidden());

				if (queryDescription == null || queryDescription.length() == 0) {
					//no description provided. use original query description
					queryDescription = report.getDescription();
				}
				newReport.setDescription(queryDescription);

				String queryName = request.getParameter("newPivotName");
				if (queryName == null || queryName.trim().length() == 0) {
					//no name provided for the new query. create a default name
					queryName = report.getName() + "-2";
				}
				newReport.setName(queryName);

				newReport.setReportGroup(report.getReportGroup());
				newReport.setDatasource(report.getDatasource());

				//save current view's mdx
				newReport.setReportSource(mdx);

				//add new report
				reportService.addReport(newReport, sessionUser);

				//give this user direct access to the view he has just created. so that he can update and overwrite it if desired
				reportService.grantAccess(report, sessionUser);

				redirectAttributes.addFlashAttribute("message", "jpivot.message.reportAdded");
				return "redirect:/success";
			}
		} catch (SQLException | RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex);
			return "redirect:/reportError";
		}
	}
}