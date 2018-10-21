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

package net.opentechnology.triki.auth.module

import net.opentechnology.triki.core.dto.PageDto
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.core.dto.TypeDto

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.filter.DelegatingFilterProxy;

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.dto.GroupDto;
import net.opentechnology.triki.core.dto.PropertyDto
import net.opentechnology.triki.core.dto.UserDto;
import net.opentechnology.triki.modules.Module
import net.opentechnology.triki.schema.Foaf
import net.opentechnology.triki.schema.Triki

public class AuthModule implements Module {
	
	public static final String path = "/auth/";
	
	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private ServletContextHandler sch;
	
	@Inject
	private GroupDto groupDto;
	
	@Inject 
	private UserDto userDto;

	@Inject
	private PageDto pageDto;

	@Inject
	private TypeDto typeDto;

	@Inject
	private SettingDto settingDto;
	
	@Inject
	private PropertyDto propertyDto;

	public enum Settings {
		INDIELOGINCLIENTID,
		INDIELOGINREDIRECTURI,
		OPENIDCONNECTREDIRECTURI,
		GOOGLEAUTHROOT,
		GOOGLECLIENTID,
		GOOGLECLIENTSECRET,
		TWITTERAUTHROOT,
		TWITTERCLIENTID,
		TWITTERCLIENTSECRET
	}

	@Override
	public void initMod() {
		initPrefixes();
		initProperties();
		initUnrestricted();
		initGroupsAndUsers();
		initSettings();
		initPages();
	}

	private void initGroupsAndUsers() {
		Resource adminGroup = groupDto.addGroup("admin", "Administrator group");
		groupDto.addGroup("private", "Private group");
		groupDto.addGroup("friends", "Friends group");
		groupDto.addGroup("family", "Family group");
		groupDto.addGroup("personal", "Personal group");
		
		def adminDetails = [:]
		adminDetails."title" = "Administrator"
		adminDetails."group" = adminGroup
		adminDetails."member" = adminGroup
		adminDetails."email" = "(unknown)"
		adminDetails."login" = "admin"
		adminDetails."password" = "admin"
		userDto.addUser("administrator", adminDetails)
	}

	private void initUnrestricted() {
		Resource authRoot = model.createResource(props.getPrivateUrl() + path);
		authRoot.addProperty(Triki.unrestricted, ".*\\.png");
		authRoot.addProperty(Triki.unrestricted, ".*\\.css");
		authRoot.addProperty(Triki.unrestricted, ".*\\.js");
		authRoot.addProperty(Triki.unrestricted, ".*\\.ico");
	}

	private void initPrefixes() {
		model.setNsPrefix("foaf", FOAF.NS);
		model.setNsPrefix("group", props.getPrivateUrl() + "group/");
		model.setNsPrefix("user", props.getPrivateUrl() + "user/");
	}
	
	private void initProperties() {
		propertyDto.addProperty("email", Foaf.mbox.getURI(), 15);
		propertyDto.addProperty("login", Triki.login.getURI(), 28);
		propertyDto.addProperty("password", Triki.password.getURI(), 29);
		propertyDto.addProperty("member", Foaf.member.getURI(), 30);
		propertyDto.addProperty("homepage", Foaf.homepage.getURI(), 16);
	}

	private void initPages() {
		pageDto.addPage("auth/indie", typeDto.getType("auth"), "Authorise IndieLogin", "public");
		pageDto.addPage("auth/openidconnect", typeDto.getType("auth"), "Authorise OpenID Connect", "public");
	}

	private void initSettings() {
		settingDto.addSetting(Settings.INDIELOGINCLIENTID.name(), "https://www.yoursite.net/", "Indie Login client ID");
		settingDto.addSetting(Settings.INDIELOGINREDIRECTURI.name(), "https://www.yoursite.net/auth/indie", "Indie Login redirect URL");
		settingDto.addSetting(Settings.OPENIDCONNECTREDIRECTURI.name(), "https://www.yoursite.net/auth/openidconnect", "OpenID Connect Redirect URL");
		settingDto.addSetting(Settings.GOOGLEAUTHROOT.name(), "https://oauth2.googleapis.com/token", "Googles OAuth2 URL");
		settingDto.addSetting(Settings.GOOGLECLIENTID.name(), "Undefined","Generated Google OAuth2 client ID");
		settingDto.addSetting(Settings.GOOGLECLIENTSECRET.name(),"Undefined","Generated Google OAuth2 client secret");
	}

	@Override
	public void initWeb() {
		sch.addFilter(new FilterHolder(new DelegatingFilterProxy("accessFilter")), "/*", EnumSet.allOf(DispatcherType.class));
	}

}
