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
package art.logger;

import art.enums.LoggerLevel;
import art.servlets.Config;
import art.general.AjaxResponse;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for logger configuration
 *
 * @author Timothy
 */
@Controller
public class LoggerController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class);

	@RequestMapping(value = "/loggers", method = RequestMethod.GET)
	public String showLoggers(Model model) {
		logger.debug("Entering showLoggers");

		if (Config.getCustomSettings().isShowErrors()) {
			//get only loggers configured in logback.xml
			//http://mailman.qos.ch/pipermail/logback-user/2008-November/000751.html
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			List<ch.qos.logback.classic.Logger> logList = new ArrayList<>();
			for (ch.qos.logback.classic.Logger log : lc.getLoggerList()) {
				if ((log.getLevel() != null && log.getLevel() != Level.OFF) || hasAppenders(log)) {
					logList.add(log);
				}
			}
			model.addAttribute("loggers", logList);
		}

		return "loggers";
	}

	/**
	 * Returns <code>true</code> if the given logback logger has appenders
	 *
	 * @param log the logback logger
	 * @return <code>true</code> if the given logback logger has appenders
	 */
	private boolean hasAppenders(ch.qos.logback.classic.Logger log) {
		Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();
		return it.hasNext();
	}

	@RequestMapping(value = "/disableLogger", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse disableLogger(@RequestParam("name") String name) {
		logger.debug("Entering disableLogger: name='{}'", name);

		AjaxResponse response = new AjaxResponse();

		ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
		log.setLevel(Level.OFF);

		response.setSuccess(true);

		return response;
	}

	@RequestMapping(value = "/addLogger", method = RequestMethod.GET)
	public String addLogger(Model model) {
		logger.debug("Entering addLogger");

		model.addAttribute("log", new art.logger.Logger());

		return showEditLogger("add", model);
	}

	@RequestMapping(value = "/saveLogger", method = RequestMethod.POST)
	public String saveLogger(@ModelAttribute("log") @Valid art.logger.Logger log,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action) {

		logger.debug("Entering saveLogger: log={}, action='{}'", log, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditLogger(action, model);
		}

		//create or edit logger
		logger.debug("log.getLevel()={}", log.getLevel());
		if (log.getLevel() != null) {
			logger.debug("log.getName()='{}'", log.getName());
			ch.qos.logback.classic.Logger newLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(log.getName()); //this creates logger if it didn't exist
			newLog.setLevel(Level.toLevel(log.getLevel().getValue()));
		}

		if (StringUtils.equals(action, "add")) {
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
		} else if (StringUtils.equals(action, "edit")) {
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
		}
		redirectAttributes.addFlashAttribute("recordName", log.getName());
		return "redirect:/loggers";
	}

	@RequestMapping(value = "/editLogger", method = RequestMethod.GET)
	public String editLogger(@RequestParam("name") String name, Model model) {
		logger.debug("Entering editLogger: name='{}'", name);

		art.logger.Logger log = new art.logger.Logger();
		log.setName(name);

		//get logger level
		ch.qos.logback.classic.Logger editLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
		logger.debug("(editLog.getLevel() != null) = {}", editLog.getLevel() != null);
		if (editLog.getLevel() != null) {
			log.setLevel(LoggerLevel.toEnum(editLog.getLevel().toString()));
		}

		model.addAttribute("log", log);

		return showEditLogger("edit", model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model
	 * @return the jsp file to display
	 */
	private String showEditLogger(String action, Model model) {
		logger.debug("Entering showEditLogger: action='{}'", action);

		model.addAttribute("levels", LoggerLevel.list());
		model.addAttribute("action", action);

		return "editLogger";
	}
}
