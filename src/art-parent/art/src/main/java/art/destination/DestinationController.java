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
package art.destination;

import art.encryption.AesEncryptor;
import art.enums.DestinationType;
import art.user.User;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Controller for destination configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class DestinationController {

	private static final Logger logger = LoggerFactory.getLogger(DestinationController.class);

	@Autowired
	private DestinationService destinationService;

	@RequestMapping(value = "/destinations", method = RequestMethod.GET)
	public String showDestinations(Model model) {
		logger.debug("Entering showDestinations");

		try {
			model.addAttribute("destinations", destinationService.getAllDestinations());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "destinations";
	}

	@RequestMapping(value = "/deleteDestination", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDestination(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDestination: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			destinationService.deleteDestination(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteDestinations", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDestinations(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteDestinations: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			destinationService.deleteDestinations(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addDestination", method = RequestMethod.GET)
	public String addDestination(Model model) {
		logger.debug("Entering addDestination");

		model.addAttribute("destination", new Destination());

		return showEditDestination("add", model);
	}

	@RequestMapping(value = "/editDestination", method = RequestMethod.GET)
	public String editDestination(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editDestination: id={}", id);

		try {
			model.addAttribute("destination", destinationService.getDestination(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination("edit", model);
	}

	@RequestMapping(value = "/editDestinations", method = RequestMethod.GET)
	public String editDestinations(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editDestinations: ids={}", ids);

		MultipleDestinationEdit multipleDestinationEdit = new MultipleDestinationEdit();
		multipleDestinationEdit.setIds(ids);

		model.addAttribute("multipleDestinationEdit", multipleDestinationEdit);

		return "editDestinations";
	}

	@RequestMapping(value = "/copyDestination", method = RequestMethod.GET)
	public String copyDestination(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyDestination: id={}", id);

		try {
			model.addAttribute("destination", destinationService.getDestination(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination("copy", model);
	}

	@RequestMapping(value = "/saveDestination", method = RequestMethod.POST)
	public String saveDestination(@ModelAttribute("destination") @Valid Destination destination,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDestination: destination={}, action='{}'", destination, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDestination(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(destination, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditDestination(action, model);
			}
			
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add") || StringUtils.equals(action, "copy")) {
				destinationService.addDestination(destination, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				destinationService.updateDestination(destination, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = destination.getName() + " (" + destination.getDestinationId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			return "redirect:/destinations";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination(action, model);
	}

	@RequestMapping(value = "/saveDestinations", method = RequestMethod.POST)
	public String saveDestinations(@ModelAttribute("multipleDestinationEdit") @Valid MultipleDestinationEdit multipleDestinationEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDestinations: multipleDestinationEdit={}", multipleDestinationEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDestinations();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			destinationService.updateDestinations(multipleDestinationEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleDestinationEdit.getIds());
			return "redirect:/destinations";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestinations();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditDestination(String action, Model model) {
		logger.debug("Entering showDestination: action='{}'", action);

		model.addAttribute("destinationTypes", DestinationType.list());
		model.addAttribute("action", action);

		return "editDestination";
	}

	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditDestinations() {
		logger.debug("Entering showEditDestinations");

		return "editDestinations";
	}

	/**
	 * Sets the password field of the ftp server
	 *
	 * @param destination the ftp server object to set
	 * @param action "add or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(Destination destination, String action) throws SQLException {
		logger.debug("Entering setPassword: destination={}, action='{}'", destination, action);

		//encrypt password
		boolean useCurrentPassword = false;
		String newPassword = destination.getPassword();

		if (destination.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			//password field blank. use current password
			Destination currentDestination = destinationService.getDestination(destination.getDestinationId());
			if (currentDestination == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newPassword = currentDestination.getPassword();
			}
		}

		//encrypt new password
		String encryptedPassword = AesEncryptor.encrypt(newPassword);
		destination.setPassword(encryptedPassword);

		//encrypt s3 access key id
		boolean useCurrentS3AccessKeyId = false;
		String newS3AccessKeyId = destination.getS3AccessKeyId();

		if (StringUtils.isEmpty(newS3AccessKeyId) && StringUtils.equals(action, "edit")) {
			//password field blank. use current password
			useCurrentS3AccessKeyId = true;
		}

		if (useCurrentS3AccessKeyId) {
			//password field blank. use current password
			Destination currentDestination = destinationService.getDestination(destination.getDestinationId());
			if (currentDestination == null) {
				return "page.message.cannotUsePassword";
			} else {
				newS3AccessKeyId = currentDestination.getS3AccessKeyId();
			}
		}

		//encrypt new password
		String encryptedS3AccessKeyId = AesEncryptor.encrypt(newS3AccessKeyId);
		destination.setS3AccessKeyId(encryptedS3AccessKeyId);

		//encrypt s3 secret access key
		boolean useCurrentS3SecretAccessKey = false;
		String newS3SecretAccessKey = destination.getS3SecretAccessKey();

		if (StringUtils.isEmpty(newS3SecretAccessKey) && StringUtils.equals(action, "edit")) {
			//password field blank. use current password
			useCurrentS3SecretAccessKey = true;
		}

		if (useCurrentS3SecretAccessKey) {
			//password field blank. use current password
			Destination currentDestination = destinationService.getDestination(destination.getDestinationId());
			if (currentDestination == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newS3SecretAccessKey = currentDestination.getS3SecretAccessKey();
			}
		}

		//encrypt new password
		String encryptedS3SecretAccessKey = AesEncryptor.encrypt(newS3SecretAccessKey);
		destination.setS3SecretAccessKey(encryptedS3SecretAccessKey);

		return null;
	}
}
