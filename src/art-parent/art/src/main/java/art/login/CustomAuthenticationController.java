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
package art.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to facilitate authentication using custom code
 * 
 * @author Timothy Anyona
 */
@Controller
public class CustomAuthenticationController {
	
	@RequestMapping("/customAuthentication")
	public String performCustomAuthentication(){
		//perform custom authentication
		//if authentication successful, set session attribute "username"
		//with the appropriate user's username and return "redirect:/reports"
		//if not, you can return "redirect:/login"
		return "redirect:/reports";
	}
	
}
