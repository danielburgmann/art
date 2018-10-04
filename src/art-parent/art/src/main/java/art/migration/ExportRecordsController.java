/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.migration;

import art.accessright.AccessRightService;
import art.accessright.UserGroupReportRight;
import art.accessright.UserReportRight;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.EncryptorType;
import art.enums.MigrationFileFormat;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import art.enums.ParameterDataType;
import art.enums.ReportType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.permission.Permission;
import art.report.Report;
import art.report.ReportService;
import art.report.ReportServiceHelper;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportoptions.C3Options;
import art.reportoptions.CsvServerOptions;
import art.reportoptions.DatamapsOptions;
import art.reportoptions.JxlsOptions;
import art.reportoptions.OrgChartOptions;
import art.reportoptions.WebMapOptions;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.role.Role;
import art.role.RoleService;
import art.rule.Rule;
import art.rule.RuleService;
import art.ruleValue.RuleValueService;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserRuleValue;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.settings.Settings;
import art.settings.SettingsService;
import art.smtpserver.SmtpServer;
import art.smtpserver.SmtpServerService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvRoutines;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for exporting repository artifacts
 *
 * @author Timothy Anyona
 */
@Controller
public class ExportRecordsController {

	private static final Logger logger = LoggerFactory.getLogger(ExportRecordsController.class);

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private EncryptorService encryptorService;

	@Autowired
	private HolidayService holidayService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private SmtpServerService smtpServerService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ScheduleService scheduleService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportParameterService reportParameterService;

	@Autowired
	private RuleValueService ruleValueService;

	@Autowired
	private ReportRuleService reportRuleService;

	@Autowired
	private AccessRightService accessRightService;

	@Autowired
	private DrilldownService drilldownService;

	@Autowired
	private RoleService roleService;

	@GetMapping("/exportRecords")
	public String showExportRecords(Model model, @RequestParam("type") String type,
			@RequestParam(value = "ids", required = false) String ids) {

		logger.debug("Entering showExportRecords: type='{}', ids='{}'", type, ids);

		ExportRecords exportRecords = new ExportRecords();
		exportRecords.setRecordType(MigrationRecordType.toEnum(type));
		exportRecords.setIds(ids);

		model.addAttribute("exportRecords", exportRecords);

		return showEditExportRecords(model);
	}

	@PostMapping("/exportRecords")
	public String processExportRecords(@Valid ExportRecords exportRecords,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering processExportRecords");

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditExportRecords(model);
		}

		try {
			MigrationRecordType recordType = exportRecords.getRecordType();
			String extension;
			switch (recordType) {
				case Settings:
					extension = ".json";
					break;
				default:
					MigrationFileFormat fileFormat = exportRecords.getFileFormat();
					switch (fileFormat) {
						case json:
							extension = ".json";
							break;
						case csv:
							extension = ".csv";
							break;
						default:
							throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
					}
			}

			String baseExportFilename = "art-export-" + recordType;
			String exportFileName = baseExportFilename + extension;
			String recordsExportPath = Config.getRecordsExportPath();
			String exportFilePath = recordsExportPath + exportFileName;

			File file = new File(exportFilePath);

			User sessionUser = (User) session.getAttribute("sessionUser");

			CsvWriterSettings writerSettings = new CsvWriterSettings();
			writerSettings.setHeaderWritingEnabled(true);
			CsvRoutines csvRoutines = new CsvRoutines(writerSettings);

			Connection conn = null;
			if (exportRecords.getLocation() == MigrationLocation.Datasource) {
				Datasource datasource = exportRecords.getDatasource();
				conn = DbConnections.getConnection(datasource.getDatasourceId());
			}

			try {
				switch (recordType) {
					case Settings:
						exportSettings(exportRecords, file, sessionUser, conn);
						break;
					case Datasources:
						exportDatasources(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Destinations:
						exportDestinations(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Encryptors:
						exportFilePath = exportEncryptors(exportRecords, sessionUser, csvRoutines, conn);
						break;
					case Holidays:
						exportHolidays(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case ReportGroups:
						exportReportGroups(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case SmtpServers:
						exportSmtpServers(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case UserGroups:
						exportFilePath = exportUserGroups(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Schedules:
						exportFilePath = exportSchedules(exportRecords, sessionUser, csvRoutines, conn, file);
						break;
					case Users:
						exportFilePath = exportUsers(exportRecords, sessionUser, csvRoutines, conn, file);
						break;
					case Rules:
						exportRules(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Parameters:
						exportFilePath = exportParameters(exportRecords, sessionUser, csvRoutines, conn);
						break;
					case Reports:
						exportFilePath = exportReports(exportRecords, sessionUser, csvRoutines, conn);
						break;
					case Roles:
						exportFilePath = exportRoles(exportRecords, sessionUser, csvRoutines, conn, file);
						break;
					default:
						break;
				}
			} finally {
				DatabaseUtils.close(conn);
			}

			if (exportRecords.getLocation() == MigrationLocation.File) {
				exportFileName = FilenameUtils.getName(exportFilePath);
				redirectAttributes.addFlashAttribute("exportFileName", exportFileName);
			}

			redirectAttributes.addFlashAttribute("message", "page.message.recordsExported");
			return "redirect:/success";
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditExportRecords(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model object
	 * @return the jsp file to display
	 */
	private String showEditExportRecords(Model model) {
		try {
			model.addAttribute("datasources", datasourceService.getActiveJdbcDatasources());
			model.addAttribute("locations", MigrationLocation.list());
			model.addAttribute("fileFormats", MigrationFileFormat.list());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "exportRecords";
	}

	/**
	 * Exports application settings
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportSettings(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportSettings");

		Settings settings = settingsService.getSettings();
		if (settings == null) {
			throw new IllegalStateException("No settings to export");
		}
		settings.encryptPasswords();

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				ObjectMapper mapper = new ObjectMapper();
				mapper.writerWithDefaultPrettyPrinter().writeValue(file, settings);
				break;
			case Datasource:
				settingsService.importSettings(settings, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports datasource records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportDatasources(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportDatasources");

		String ids = exportRecords.getIds();
		List<Datasource> datasources = datasourceService.getDatasources(ids);
		for (Datasource datasource : datasources) {
			datasource.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, datasources);
						break;
					case csv:
						csvRoutines.writeAll(datasources, Datasource.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				datasourceService.importDatasources(datasources, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports destination records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportDestinations(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportDestinations");

		String ids = exportRecords.getIds();
		List<Destination> destinations = destinationService.getDestinations(ids);
		for (Destination destination : destinations) {
			destination.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, destinations);
						break;
					case csv:
						csvRoutines.writeAll(destinations, Destination.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				destinationService.importDestinations(destinations, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports encryptor records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportEncryptors(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportEncryptors");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Encryptor> encryptors = encryptorService.getEncryptors(ids);
		for (Encryptor encryptor : encryptors) {
			encryptor.encryptPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Encryptors.zip";
				String encryptorsFilePath;
				File encryptorsFile;

				List<String> filesToZip = getEncryptorFiles(encryptors);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						encryptorsFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_ENCRYPTORS_FILENAME;
						encryptorsFile = new File(encryptorsFilePath);
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(encryptorsFile, encryptors);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(encryptorsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							encryptorsFile.delete();
						} else {
							exportFilePath = encryptorsFilePath;
						}
						break;
					case csv:
						encryptorsFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_ENCRYPTORS_FILENAME;
						encryptorsFile = new File(encryptorsFilePath);
						csvRoutines.writeAll(encryptors, Encryptor.class, encryptorsFile);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(encryptorsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							encryptorsFile.delete();
						} else {
							exportFilePath = encryptorsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				encryptorService.importEncryptors(encryptors, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports holiday records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportHolidays(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportHolidays");

		String ids = exportRecords.getIds();
		List<Holiday> holidays = holidayService.getHolidays(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, holidays);
						break;
					case csv:
						csvRoutines.writeAll(holidays, Holiday.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				holidayService.importHolidays(holidays, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports report group records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportReportGroups(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportReportGroups");

		String ids = exportRecords.getIds();
		List<ReportGroup> reportGroups = reportGroupService.getReportGroups(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, reportGroups);
						break;
					case csv:
						csvRoutines.writeAll(reportGroups, ReportGroup.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				reportGroupService.importReportGroups(reportGroups, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports smtp server records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportSmtpServers(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportSmtpServers");

		String ids = exportRecords.getIds();
		List<SmtpServer> smtpServers = smtpServerService.getSmtpServers(ids);
		for (SmtpServer smtpServer : smtpServers) {
			smtpServer.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, smtpServers);
						break;
					case csv:
						csvRoutines.writeAll(smtpServers, SmtpServer.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				smtpServerService.importSmtpServers(smtpServers, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports user group records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportUserGroups(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportUserGroups");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<UserGroup> userGroups = userGroupService.getUserGroups(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, userGroups);
						exportFilePath = recordsExportPath + "art-export-UserGroups.json";
						break;
					case csv:
						String userGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
						File userGroupsFile = new File(userGroupsFilePath);
						csvRoutines.writeAll(userGroups, UserGroup.class, userGroupsFile);

						List<Role> allRoles = new ArrayList<>();
						for (UserGroup userGroup : userGroups) {
							List<Role> roles = userGroup.getRoles();
							for (Role role : roles) {
								role.setParentId(userGroup.getUserGroupId());
								allRoles.add(role);
							}
						}

						List<Permission> allPermissions = new ArrayList<>();
						for (UserGroup userGroup : userGroups) {
							List<Permission> permissions = userGroup.getPermissions();
							for (Permission permission : permissions) {
								permission.setParentId(userGroup.getUserGroupId());
								allPermissions.add(permission);
							}
						}

						if (CollectionUtils.isNotEmpty(allRoles)
								|| CollectionUtils.isNotEmpty(allPermissions)) {
							List<String> filesToZip = new ArrayList<>();
							filesToZip.add(userGroupsFilePath);

							String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
							File rolesFile = new File(rolesFilePath);
							if (CollectionUtils.isNotEmpty(allRoles)) {
								csvRoutines.writeAll(allRoles, Role.class, rolesFile);
								filesToZip.add(rolesFilePath);
							}

							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							if (CollectionUtils.isNotEmpty(allPermissions)) {
								csvRoutines.writeAll(allPermissions, Permission.class, permissionsFile);
								filesToZip.add(permissionsFilePath);
							}

							exportFilePath = recordsExportPath + "art-export-UserGroups.zip";
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							userGroupsFile.delete();
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = userGroupsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				userGroupService.importUserGroups(userGroups, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports schedule records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportSchedules(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn, File file)
			throws SQLException, IOException {

		logger.debug("Entering exportSchedules");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Schedule> schedules = scheduleService.getSchedules(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, schedules);
						exportFilePath = recordsExportPath + "art-export-Schedules.json";
						break;
					case csv:
						String schedulesFilePath = recordsExportPath + ExportRecords.EMBEDDED_SCHEDULES_FILENAME;
						File schedulesFile = new File(schedulesFilePath);
						csvRoutines.writeAll(schedules, Schedule.class, schedulesFile);
						List<Holiday> holidays = new ArrayList<>();
						for (Schedule schedule : schedules) {
							List<Holiday> sharedHolidays = schedule.getSharedHolidays();
							for (Holiday holiday : sharedHolidays) {
								holiday.setParentId(schedule.getScheduleId());
								holidays.add(holiday);
							}
						}
						if (CollectionUtils.isNotEmpty(holidays)) {
							String holidaysFilePath = recordsExportPath + ExportRecords.EMBEDDED_HOLIDAYS_FILENAME;
							File holidaysFile = new File(holidaysFilePath);
							csvRoutines.writeAll(holidays, Holiday.class, holidaysFile);
							exportFilePath = recordsExportPath + "art-export-Schedules.zip";
							ArtUtils.zipFiles(exportFilePath, schedulesFilePath, holidaysFilePath);
							schedulesFile.delete();
							holidaysFile.delete();
						} else {
							exportFilePath = schedulesFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				scheduleService.importSchedules(schedules, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports user records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportUsers(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn, File file)
			throws SQLException, IOException {

		logger.debug("Entering exportUsers");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<User> users = userService.getUsers(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
						exportFilePath = recordsExportPath + "art-export-Users.json";
						break;
					case csv:
						String usersFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERS_FILENAME;
						File usersFile = new File(usersFilePath);
						csvRoutines.writeAll(users, User.class, usersFile);

						List<UserGroup> allUserGroups = new ArrayList<>();
						for (User user : users) {
							List<UserGroup> userGroups = user.getUserGroups();
							for (UserGroup userGroup : userGroups) {
								userGroup.setParentId(user.getUserId());
								allUserGroups.add(userGroup);
							}
						}

						List<Role> allRoles = new ArrayList<>();
						for (User user : users) {
							List<Role> roles = user.getRoles();
							for (Role role : roles) {
								role.setParentId(user.getUserId());
								allRoles.add(role);
							}
						}

						List<Permission> allPermissions = new ArrayList<>();
						for (User user : users) {
							List<Permission> permissions = user.getPermissions();
							for (Permission permission : permissions) {
								permission.setParentId(user.getUserId());
								allPermissions.add(permission);
							}
						}

						if (CollectionUtils.isNotEmpty(allUserGroups)
								|| CollectionUtils.isNotEmpty(allRoles)
								|| CollectionUtils.isNotEmpty(allPermissions)) {
							List<String> filesToZip = new ArrayList<>();
							filesToZip.add(usersFilePath);

							String userGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
							File userGroupsFile = new File(userGroupsFilePath);
							if (CollectionUtils.isNotEmpty(allUserGroups)) {
								csvRoutines.writeAll(allUserGroups, UserGroup.class, userGroupsFile);
								filesToZip.add(userGroupsFilePath);
							}

							String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
							File rolesFile = new File(rolesFilePath);
							if (CollectionUtils.isNotEmpty(allRoles)) {
								csvRoutines.writeAll(allRoles, Role.class, rolesFile);
								filesToZip.add(rolesFilePath);
							}

							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							if (CollectionUtils.isNotEmpty(allPermissions)) {
								csvRoutines.writeAll(allPermissions, Permission.class, permissionsFile);
								filesToZip.add(permissionsFilePath);
							}

							exportFilePath = recordsExportPath + "art-export-Users.zip";
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							usersFile.delete();
							userGroupsFile.delete();
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = usersFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				userService.importUsers(users, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports rule records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportRules(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportRules");

		String ids = exportRecords.getIds();
		List<Rule> rules = ruleService.getRules(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, rules);
						break;
					case csv:
						csvRoutines.writeAll(rules, Rule.class, file);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				ruleService.importRules(rules, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports parameter records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportParameters(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportParameters");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Parameter> parameters = parameterService.getParameters(ids);

		for (Parameter parameter : parameters) {
			parameter.encryptAllPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Parameters.zip";
				String parametersFilePath;
				File parametersFile;

				List<String> filesToZip = getParameterTemplateFiles(parameters);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						parametersFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_PARAMETERS_FILENAME;
						parametersFile = new File(parametersFilePath);
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(parametersFile, parameters);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(parametersFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							parametersFile.delete();
						} else {
							exportFilePath = parametersFilePath;
						}
						break;
					case csv:
						parametersFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_PARAMETERS_FILENAME;
						parametersFile = new File(parametersFilePath);
						csvRoutines.writeAll(parameters, Parameter.class, parametersFile);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(parametersFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							parametersFile.delete();
						} else {
							exportFilePath = parametersFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean local = false;
				parameterService.importParameters(parameters, sessionUser, conn, local);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports report records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportReports(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportReports");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Report> reports = reportService.getReports(ids);

		List<ReportGroup> allReportGroups = new ArrayList<>();
		List<ReportParameter> allReportParams = new ArrayList<>();
		List<UserRuleValue> allUserRuleValues = new ArrayList<>();
		List<UserGroupRuleValue> allUserGroupRuleValues = new ArrayList<>();
		List<ReportRule> allReportRules = new ArrayList<>();
		List<UserReportRight> allUserReportRights = new ArrayList<>();
		List<UserGroupReportRight> allUserGroupReportRights = new ArrayList<>();
		List<Drilldown> allDrilldowns = new ArrayList<>();
		List<ReportParameter> allDrilldownReportParams = new ArrayList<>();

		for (Report report : reports) {
			int reportId = report.getReportId();

			List<ReportGroup> reportGroups = report.getReportGroups();
			for (ReportGroup reportGroup : reportGroups) {
				reportGroup.setParentId(reportId);
				allReportGroups.add(reportGroup);
			}

			List<ReportParameter> reportParams = reportParameterService.getReportParameters(reportId);
			report.setReportParams(reportParams);
			for (ReportParameter reportParam : reportParams) {
				reportParam.setParentId(reportId);
				allReportParams.add(reportParam);
			}

			List<UserRuleValue> userRuleValues = ruleValueService.getReportUserRuleValues(reportId);
			report.setUserRuleValues(userRuleValues);
			for (UserRuleValue userRuleValue : userRuleValues) {
				userRuleValue.setParentId(reportId);
				allUserRuleValues.add(userRuleValue);
			}

			List<UserGroupRuleValue> userGroupRuleValues = ruleValueService.getReportUserGroupRuleValues(reportId);
			report.setUserGroupRuleValues(userGroupRuleValues);
			for (UserGroupRuleValue userGroupRuleValue : userGroupRuleValues) {
				userGroupRuleValue.setParentId(reportId);
				allUserGroupRuleValues.add(userGroupRuleValue);
			}

			List<ReportRule> reportRules = reportRuleService.getReportRules(reportId);
			report.setReportRules(reportRules);
			for (ReportRule reportRule : reportRules) {
				reportRule.setParentId(reportId);
				allReportRules.add(reportRule);
			}

			List<UserReportRight> userReportRights = accessRightService.getUserReportRightsForReport(reportId);
			report.setUserReportRights(userReportRights);
			for (UserReportRight userReportRight : userReportRights) {
				userReportRight.setParentId(reportId);
				allUserReportRights.add(userReportRight);
			}

			List<UserGroupReportRight> userGroupReportRights = accessRightService.getUserGroupReportRightsForReport(reportId);
			report.setUserGroupReportRights(userGroupReportRights);
			for (UserGroupReportRight userGroupReportRight : userGroupReportRights) {
				userGroupReportRight.setParentId(reportId);
				allUserGroupReportRights.add(userGroupReportRight);
			}

			List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
			report.setDrilldowns(drilldowns);
			for (Drilldown drilldown : drilldowns) {
				drilldown.setParentId(reportId);
				allDrilldowns.add(drilldown);

				Report drilldownReport = drilldown.getDrilldownReport();
				int drilldownReportId = drilldownReport.getReportId();
				List<ReportParameter> drilldownReportParams = reportParameterService.getReportParameters(drilldownReportId);
				for (ReportParameter drilldownReportParam : drilldownReportParams) {
					drilldownReportParam.setParentId(drilldownReportId);
					allDrilldownReportParams.add(drilldownReportParam);
				}
			}
		}

		for (Report report : reports) {
			report.encryptAllPasswords();
		}

		for (Drilldown drilldown : allDrilldowns) {
			Report drilldownReport = drilldown.getDrilldownReport();
			drilldownReport.encryptAllPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Reports.zip";
				String reportsFilePath;
				File reportsFile;

				List<String> filesToZip = getReportTemplateFiles(reports);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						reportsFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_REPORTS_FILENAME;
						reportsFile = new File(reportsFilePath);
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(reportsFile, reports);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(reportsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							reportsFile.delete();
						} else {
							exportFilePath = reportsFilePath;
						}
						break;
					case csv:
						reportsFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_REPORTS_FILENAME;
						reportsFile = new File(reportsFilePath);
						csvRoutines.writeAll(reports, Report.class, reportsFile);

						if (CollectionUtils.isNotEmpty(allReportGroups)
								|| CollectionUtils.isNotEmpty(allReportParams)
								|| CollectionUtils.isNotEmpty(allUserRuleValues)
								|| CollectionUtils.isNotEmpty(allUserGroupRuleValues)
								|| CollectionUtils.isNotEmpty(allReportRules)
								|| CollectionUtils.isNotEmpty(allUserReportRights)
								|| CollectionUtils.isNotEmpty(allUserGroupReportRights)
								|| CollectionUtils.isNotEmpty(allDrilldowns)
								|| CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(reportsFilePath);

							String reportGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTGROUPS_FILENAME;
							File reportGroupsFile = new File(reportGroupsFilePath);

							String reportParamsFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTPARAMETERS_FILENAME;
							File reportParamsFile = new File(reportParamsFilePath);

							String userRuleValuesFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERRULEVALUES_FILENAME;
							File userRuleValuesFile = new File(userRuleValuesFilePath);

							String userGroupRuleValuesFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPRULEVALUES_FILENAME;
							File userGroupRuleValuesFile = new File(userGroupRuleValuesFilePath);

							String reportRulesFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTRULES_FILENAME;
							File reportRulesFile = new File(reportRulesFilePath);

							String userReportRightsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERREPORTRIGHTS_FILENAME;
							File userReportRightsFile = new File(userReportRightsFilePath);

							String userGroupReportRightsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME;
							File userGroupReportRightsFile = new File(userGroupReportRightsFilePath);

							String drilldownsFilePath = recordsExportPath + ExportRecords.EMBEDDED_DRILLDOWNS_FILENAME;
							File drilldownsFile = new File(drilldownsFilePath);

							String drilldownReportParamsFilePath = recordsExportPath + ExportRecords.EMBEDDED_DRILLDOWNREPORTPARAMETERS_FILENAME;
							File drilldownReportParamsFile = new File(drilldownReportParamsFilePath);

							if (CollectionUtils.isNotEmpty(allReportGroups)) {
								csvRoutines.writeAll(allReportGroups, ReportGroup.class, reportGroupsFile);
								filesToZip.add(reportGroupsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allReportParams)) {
								csvRoutines.writeAll(allReportParams, ReportParameter.class, reportParamsFile);
								filesToZip.add(reportParamsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserRuleValues)) {
								csvRoutines.writeAll(allUserRuleValues, UserRuleValue.class, userRuleValuesFile);
								filesToZip.add(userRuleValuesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserGroupRuleValues)) {
								csvRoutines.writeAll(allUserGroupRuleValues, UserGroupRuleValue.class, userGroupRuleValuesFile);
								filesToZip.add(userGroupRuleValuesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allReportRules)) {
								csvRoutines.writeAll(allReportRules, ReportRule.class, reportRulesFile);
								filesToZip.add(reportRulesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserReportRights)) {
								csvRoutines.writeAll(allUserReportRights, UserReportRight.class, userReportRightsFile);
								filesToZip.add(userReportRightsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserGroupReportRights)) {
								csvRoutines.writeAll(allUserGroupReportRights, UserGroupReportRight.class, userGroupReportRightsFile);
								filesToZip.add(userGroupReportRightsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allDrilldowns)) {
								csvRoutines.writeAll(allDrilldowns, Drilldown.class, drilldownsFile);
								filesToZip.add(drilldownsFilePath);

								if (CollectionUtils.isNotEmpty(allDrilldownReportParams)) {
									csvRoutines.writeAll(allDrilldownReportParams, ReportParameter.class, drilldownReportParamsFile);
									filesToZip.add(drilldownReportParamsFilePath);
								}
							}

							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							reportsFile.delete();
							reportGroupsFile.delete();
							reportParamsFile.delete();
							userRuleValuesFile.delete();
							userGroupRuleValuesFile.delete();
							reportRulesFile.delete();
							userReportRightsFile.delete();
							userGroupReportRightsFile.delete();
							drilldownsFile.delete();
							drilldownReportParamsFile.delete();
						} else {
							exportFilePath = reportsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				ReportServiceHelper reportServiceHelper = new ReportServiceHelper();
				boolean local = false;
				reportServiceHelper.importReports(reports, sessionUser, conn, local);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Returns full paths of template files to include in the final zip package
	 *
	 * @param reports the reports being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getReportTemplateFiles(List<Report> reports) throws IOException {
		List<String> filesToZip = new ArrayList<>();

		for (Report report : reports) {
			ReportType reportType = report.getReportType();
			if (reportType == null) {
				logger.warn("reportType is null. Report={}", report);
			} else {
				String template = report.getTemplate();
				if (StringUtils.isNotBlank(template)) {
					String templatesPath;
					if (reportType.isUseJsTemplatesPath()) {
						templatesPath = Config.getJsTemplatesPath();
					} else if (reportType == ReportType.JPivotMondrian) {
						templatesPath = Config.getDefaultTemplatesPath();
					} else {
						templatesPath = Config.getTemplatesPath();
					}
					String templateFilePath = templatesPath + template;
					File templateFile = new File(templateFilePath);
					if (templateFile.exists() && !filesToZip.contains(templateFilePath)) {
						filesToZip.add(templateFilePath);
					}
				}

				String options = report.getOptions();
				if (StringUtils.isNotBlank(options)) {
					switch (reportType) {
						case JxlsArt:
						case JxlsTemplate:
							JxlsOptions jxlsOptions = ArtUtils.jsonToObject(options, JxlsOptions.class);
							String areaConfigFilename = jxlsOptions.getAreaConfigFile();
							if (StringUtils.isNotBlank(areaConfigFilename)) {
								String templatesPath = Config.getTemplatesPath();
								String fullAreaConfigFilename = templatesPath + areaConfigFilename;
								File areaConfigFile = new File(fullAreaConfigFilename);
								if (areaConfigFile.exists() && !filesToZip.contains(fullAreaConfigFilename)) {
									filesToZip.add(fullAreaConfigFilename);
								}
							}
							break;
						case PivotTableJsCsvServer:
						case DygraphsCsvServer:
						case DataTablesCsvServer:
							CsvServerOptions csvServerOptions = ArtUtils.jsonToObject(options, CsvServerOptions.class);
							String dataFileName = csvServerOptions.getDataFile();
							if (StringUtils.isNotBlank(dataFileName)) {
								String jsTemplatesPath = Config.getJsTemplatesPath();
								String fullDataFileName = jsTemplatesPath + dataFileName;
								File dataFile = new File(fullDataFileName);
								if (dataFile.exists() && !filesToZip.contains(fullDataFileName)) {
									filesToZip.add(fullDataFileName);
								}
							}
							break;
						case C3:
							C3Options c3Options = ArtUtils.jsonToObject(options, C3Options.class);
							String cssFileName = c3Options.getCssFile();
							if (StringUtils.isNotBlank(cssFileName)) {
								String jsTemplatesPath = Config.getJsTemplatesPath();
								String fullCssFileName = jsTemplatesPath + cssFileName;
								File cssFile = new File(fullCssFileName);
								if (cssFile.exists() && !filesToZip.contains(fullCssFileName)) {
									filesToZip.add(fullCssFileName);
								}
							}
							break;
						case Datamaps:
						case DatamapsFile:
							DatamapsOptions datamapsOptions = ArtUtils.jsonToObject(options, DatamapsOptions.class);
							String jsTemplatesPath = Config.getJsTemplatesPath();

							String datamapsJsFileName = datamapsOptions.getDatamapsJsFile();
							if (StringUtils.isNotBlank(datamapsJsFileName)) {
								String fullDatamapsJsFileName = jsTemplatesPath + datamapsJsFileName;
								File datamapsJsFile = new File(fullDatamapsJsFileName);
								if (datamapsJsFile.exists() && !filesToZip.contains(fullDatamapsJsFileName)) {
									filesToZip.add(fullDatamapsJsFileName);
								}
							}

							dataFileName = datamapsOptions.getDataFile();
							if (StringUtils.isNotBlank(dataFileName)) {
								String fullDataFileName = jsTemplatesPath + dataFileName;
								File dataFile = new File(fullDataFileName);
								if (dataFile.exists() && !filesToZip.contains(fullDataFileName)) {
									filesToZip.add(fullDataFileName);
								}
							}

							String mapFileName = datamapsOptions.getMapFile();
							if (StringUtils.isNotBlank(mapFileName)) {
								String fullMapFileName = jsTemplatesPath + mapFileName;
								File mapFile = new File(fullMapFileName);
								if (mapFile.exists() && !filesToZip.contains(fullMapFileName)) {
									filesToZip.add(fullMapFileName);
								}
							}

							cssFileName = datamapsOptions.getCssFile();
							if (StringUtils.isNotBlank(cssFileName)) {
								String fullCssFileName = jsTemplatesPath + cssFileName;
								File cssFile = new File(fullCssFileName);
								if (cssFile.exists() && !filesToZip.contains(fullCssFileName)) {
									filesToZip.add(fullCssFileName);
								}
							}
							break;
						case Leaflet:
						case OpenLayers:
							WebMapOptions webMapOptions = ArtUtils.jsonToObject(options, WebMapOptions.class);
							jsTemplatesPath = Config.getJsTemplatesPath();

							cssFileName = webMapOptions.getCssFile();
							if (StringUtils.isNotBlank(cssFileName)) {
								String fullCssFileName = jsTemplatesPath + cssFileName;
								File cssFile = new File(fullCssFileName);
								if (cssFile.exists() && !filesToZip.contains(fullCssFileName)) {
									filesToZip.add(fullCssFileName);
								}
							}

							dataFileName = webMapOptions.getDataFile();
							if (StringUtils.isNotBlank(dataFileName)) {
								String fullDataFileName = jsTemplatesPath + dataFileName;
								File dataFile = new File(fullDataFileName);
								if (dataFile.exists() && !filesToZip.contains(fullDataFileName)) {
									filesToZip.add(fullDataFileName);
								}
							}

							List<String> jsFileNames = webMapOptions.getJsFiles();
							if (CollectionUtils.isNotEmpty(jsFileNames)) {
								for (String jsFileName : jsFileNames) {
									if (StringUtils.isNotBlank(jsFileName)) {
										String fullJsFileName = jsTemplatesPath + jsFileName;
										File jsFile = new File(fullJsFileName);
										if (jsFile.exists() && !filesToZip.contains(fullJsFileName)) {
											filesToZip.add(fullJsFileName);
										}
									}
								}
							}

							List<String> cssFileNames = webMapOptions.getCssFiles();
							if (CollectionUtils.isNotEmpty(cssFileNames)) {
								for (String listCssFileName : cssFileNames) {
									if (StringUtils.isNotBlank(listCssFileName)) {
										String fullListCssFileName = jsTemplatesPath + listCssFileName;
										File listCssFile = new File(fullListCssFileName);
										if (listCssFile.exists() && !filesToZip.contains(fullListCssFileName)) {
											filesToZip.add(fullListCssFileName);
										}
									}
								}
							}
							break;
						case OrgChartDatabase:
						case OrgChartJson:
						case OrgChartList:
						case OrgChartAjax:
							OrgChartOptions orgChartOptions = ArtUtils.jsonToObject(options, OrgChartOptions.class);
							jsTemplatesPath = Config.getJsTemplatesPath();

							cssFileName = orgChartOptions.getCssFile();
							if (StringUtils.isNotBlank(cssFileName)) {
								String fullCssFileName = jsTemplatesPath + cssFileName;
								File cssFile = new File(fullCssFileName);
								if (cssFile.exists() && !filesToZip.contains(fullCssFileName)) {
									filesToZip.add(fullCssFileName);
								}
							}
							break;
						default:
							break;
					}
				}
			}
		}

		return filesToZip;
	}

	/**
	 * Returns full paths of template files to include in the final zip package
	 *
	 * @param parameters the parameters being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getParameterTemplateFiles(List<Parameter> parameters) throws IOException {
		List<String> filesToZip = new ArrayList<>();

		for (Parameter parameter : parameters) {
			ParameterDataType dataType = parameter.getDataType();
			if (dataType == null) {
				logger.warn("dataType is null. Parameter={}", parameter);
			} else {
				switch (dataType) {
					case DateRange:
						String template = parameter.getTemplate();
						if (StringUtils.isNotBlank(template)) {
							String jsTemplatesPath = Config.getJsTemplatesPath();
							String templateFilePath = jsTemplatesPath + template;
							File templateFile = new File(templateFilePath);
							if (templateFile.exists() && !filesToZip.contains(templateFilePath)) {
								filesToZip.add(templateFilePath);
							}
						}
						break;
					default:
						break;
				}
			}
		}

		return filesToZip;
	}

	/**
	 * Returns full paths of encryptor files to include in the final zip package
	 *
	 * @param encryptors the encryptors being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getEncryptorFiles(List<Encryptor> encryptors) throws IOException {
		List<String> filesToZip = new ArrayList<>();

		for (Encryptor encryptor : encryptors) {
			EncryptorType encryptorType = encryptor.getEncryptorType();
			if (encryptorType == null) {
				logger.warn("encryptorType is null. Encryptor={}", encryptor);
			} else {
				switch (encryptorType) {
					case OpenPGP:
						String publicKeyFileName = encryptor.getOpenPgpPublicKeyFile();
						if (StringUtils.isNotBlank(publicKeyFileName)) {
							String templatesPath = Config.getTemplatesPath();
							String publicKeyPath = templatesPath + publicKeyFileName;
							File publicKeyFile = new File(publicKeyPath);
							if (publicKeyFile.exists() && !filesToZip.contains(publicKeyPath)) {
								filesToZip.add(publicKeyPath);
							}
						}
						break;
					default:
						break;
				}
			}
		}

		return filesToZip;
	}

	/**
	 * Exports role records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportRoles(ExportRecords exportRecords,
			User sessionUser, CsvRoutines csvRoutines, Connection conn, File file)
			throws SQLException, IOException {

		logger.debug("Entering exportRoles");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Role> roles = roleService.getRoles(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter().writeValue(file, roles);
						exportFilePath = recordsExportPath + "art-export-Roles.json";
						break;
					case csv:
						String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
						File rolesFile = new File(rolesFilePath);
						csvRoutines.writeAll(roles, Role.class, rolesFile);
						List<Permission> permissions = new ArrayList<>();
						for (Role role : roles) {
							List<Permission> rolePermissions = role.getPermissions();
							for (Permission permission : rolePermissions) {
								permission.setParentId(role.getRoleId());
								permissions.add(permission);
							}
						}
						if (CollectionUtils.isNotEmpty(permissions)) {
							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							csvRoutines.writeAll(permissions, Permission.class, permissionsFile);
							exportFilePath = recordsExportPath + "art-export-Roles.zip";
							ArtUtils.zipFiles(exportFilePath, rolesFilePath, permissionsFilePath);
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = rolesFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				roleService.importRoles(roles, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

}
