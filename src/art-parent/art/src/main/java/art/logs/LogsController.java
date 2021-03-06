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
package art.logs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the logs page
 *
 * @author Timothy Anyona
 */
@Controller
public class LogsController {

	private static final Logger logger = LoggerFactory.getLogger(LogsController.class);

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/logs")
	public String showLogs(Model model, Locale locale) {
		logger.debug("Entering showLogs");

		final String CYCLIC_BUFFER_APPENDER_NAME = "CYCLIC"; //name of cyclic appender in logback.xml

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) context.getLogger(
				Logger.ROOT_LOGGER_NAME).getAppender(CYCLIC_BUFFER_APPENDER_NAME);

		int count = -1;
		logger.debug("cyclicBufferAppender != null = {}", cyclicBufferAppender != null);
		if (cyclicBufferAppender != null) {
			count = cyclicBufferAppender.getLength();

			if (count == 0) {
				model.addAttribute("message", "logs.message.noLoggingEvents");
			} else if (count > 0) {
				String causedByText = messageSource.getMessage("logs.text.causedBy", null, locale);
				String commonFramesOmittedText = messageSource.getMessage("logs.text.commonFramesOmitted", null, locale);

				List<LogbackLoggingEvent> logs = new ArrayList<>();

				for (int i = 0; i < count; i++) {
					ILoggingEvent loggingEvent = cyclicBufferAppender.get(i);
					LogbackLoggingEvent log = LogbackLoggingEvent.build(loggingEvent);
					log.setCausedByText(causedByText);
					log.setCommonFramesOmittedText(commonFramesOmittedText);
					logs.add(log);
				}

				model.addAttribute("logs", logs);
			}
		}

		logger.debug("count={}", count);
		if (count == -1) {
			model.addAttribute("message", "logs.message.appenderNotFound");
		}

		return "logs";
	}
}
