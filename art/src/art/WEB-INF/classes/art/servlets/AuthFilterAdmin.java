/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory); 
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
package art.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter applied on accessing admin directory files
 *
 * @author Enrico Liboni
 */
public final class AuthFilterAdmin implements Filter {

	final static Logger logger = LoggerFactory.getLogger(AuthFilterAdmin.class);
	private FilterConfig filterConfig = null;

	/**
	 *
	 */
	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	/**
	 *
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	/**
	 * Check if this is a valid admin session
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			HttpServletRequest hrequest = (HttpServletRequest) request;
			HttpServletResponse hresponse = (HttpServletResponse) response;
			HttpSession session = hrequest.getSession();

			if (ArtDBCP.isArtSettingsLoaded()) {
				// settings are defined
				if (session.getAttribute("AdminSession") != null) {
					// if the admin connection is not in the session 
					// get the connection and store it in the admin session 
					if (session.getAttribute("SessionConn") == null) {
						try {
							session.setAttribute("SessionConn", ArtDBCP.getAdminConnection());
						} catch (Exception e) {
							logger.error("Error while getting the connection to the ART repository", e);
							PrintWriter out = response.getWriter();
							out.println("<html> Error while getting the connection to the ART repository<br><code>" + e + "</code></html>");
						}
					}
					chain.doFilter(request, response);
				} else {
					//check if we have come from a login page
					String username = hrequest.getParameter("username");
					String password = hrequest.getParameter("password");
					String sessionUser = (String) session.getAttribute("username");
					if (username != null || (username==null && sessionUser!=null)) {
						//we have come from a login page. authenticate
						boolean isArtRepositoryUser = false;
						try {
							String msg = ArtDBCP.authenticateSession(hrequest);
							if (msg == null) {
								//no error messages. authentication succeeded
								if (StringUtils.equals(username, ArtDBCP.getArtRepositoryUsername())
										&& StringUtils.equals(password, ArtDBCP.getArtRepositoryPassword()) && StringUtils.isNotBlank(username)) {
									// using repository username and password
									isArtRepositoryUser = true;
								}
								if (isArtRepositoryUser) {
									hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/admin/adminConsole.jsp"));
									return; //not needed but retained in case code changes later giving execution path after redirect
								} else {
									// auth ok
									//ensure this is an admin user
									if (session.getAttribute("AdminSession") != null) {
										//this is an admin user
										chain.doFilter(request, response);
									} else {
										//this is not an admin user. go to start page
										hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/user/showGroups.jsp"));
										return; //not needed but retained in case code changes later giving execution path after redirect
									}
								}
							} else {
								//authentication failed. display error message
								//remember the page the user tried to access in order to forward after the authentication
								String nextPage = hrequest.getRequestURI();
								if (hrequest.getQueryString() != null) {
									nextPage = nextPage + "?" + hrequest.getQueryString();
								}
								session.setAttribute("nextPage", nextPage);

								//display appropriate login page
								forwardToLoginPage(hresponse, hrequest, msg);
							}
						} catch (Exception e) {
							logger.error("Error", e);
							forwardToLoginPage(hresponse, hrequest, e.getMessage());
						}
					} else {
						//we have not come from a login page. display appropriate login page
						//remember the page the user tried to access in order to forward after the authentication
						String nextPage = hrequest.getRequestURI();
						if (hrequest.getQueryString() != null) {
							nextPage = nextPage + "?" + hrequest.getQueryString();
						}
						session.setAttribute("nextPage", nextPage);

						java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages", hrequest.getLocale());
						forwardToLoginPage(hresponse, hrequest, messages.getString("sessionExpired"));
					}
				}

			} else {
				// settings are not defined - this is the 1st logon (see execLogin.jsp)
				session.setAttribute("AdminSession", "Y");
				session.setAttribute("AdminLevel", new Integer(100));
				session.setAttribute("AdminUsername", "art");
				chain.doFilter(request, response);
			}
		}
	}

	/**
	 * Forward to the appropriate login page.
	 *
	 * @param hresponse http response
	 * @param hrequest http request
	 * @param msg message to display
	 * @throws ServletException
	 * @throws IOException
	 */
	private void forwardToLoginPage(HttpServletResponse hresponse, HttpServletRequest hrequest, String msg) throws ServletException, IOException {
		hrequest.setAttribute("message", msg);
		String toPage = ArtDBCP.getArtSetting("index_page_default");
		if (toPage == null || toPage.equals("default")) {
			toPage = "login";
		}
		hrequest.getRequestDispatcher("/" + toPage + ".jsp").forward(hrequest, hresponse);
	}
}
