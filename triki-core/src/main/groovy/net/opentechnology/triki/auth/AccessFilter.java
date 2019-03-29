/************************************************************************************
*
*   This file is part of triki
*
*   Written by Donald McIntosh (dbm@opentechnology.net) 
*
*   triki is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   triki is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with triki.  If not, see <http://www.gnu.org/licenses/>.
*
************************************************************************************/

package net.opentechnology.triki.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.opentechnology.triki.auth.module.AuthModule;
import net.opentechnology.triki.core.dto.SettingDto;
import org.apache.commons.configuration.Configuration;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import net.opentechnology.triki.core.boot.CachedPropertyStore;

public class AccessFilter implements Filter {
	@Inject	@Qualifier("siteModel")
    private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private AuthorisationManager authoriser;

	@Inject
	private SettingDto settingDto;

	protected final Logger logger = Logger.getLogger(this.getClass());


	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpSession session = httpReq.getSession();
		String path = httpReq.getRequestURI();
		
		String url = props.getPrivateUrl() + path.replaceFirst("/", "");
		logger.debug("Checking auth on " + url);
		if(!authoriser.allowAccess(url)){		
			session.setAttribute("redirectUrl", url);
			RequestDispatcher dispatcher = request.getRequestDispatcher(settingDto.getSetting(AuthModule.Settings.DEFAULTLOGINPAGE.toString()));
			dispatcher.forward(request, response);
			return;
		}
		
		chain.doFilter(request, response);
	}

	public void destroy() {

	}

}
