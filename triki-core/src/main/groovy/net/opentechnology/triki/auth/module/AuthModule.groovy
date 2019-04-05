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


import net.opentechnology.triki.core.dto.IdentityProviderDto
import net.opentechnology.triki.core.dto.PageDto
import net.opentechnology.triki.core.dto.PrefixDto
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

	@Inject
	private PrefixDto prefixDto;

	@Inject
	private IdentityProviderDto identityProviderDto;

	public enum Settings {
		OPENIDCONNECTREDIRECTURI,
		INDIELOGINROOT,
		INDIELOGINCLIENTID,
		INDIELOGINREDIRECTURI,
		TWITTERAUTHENDPOINT,
		TWITTERCLIENTID,
		TWITTERCLIENTSECRET,
		YAHOOAUTHENDPOINT,
		YAHOOTOKENENDPOINT,
		YAHOOCLIENTID,
		YAHOOCLIENTSECRET,
		YAHOOOPENIDSCOPE,
		YAHOOPROFILEENDPOINT,
		AMAZONAUTHENDPOINT,
		AMAZONTOKENENDPOINT,
		AMAZONCLIENTID,
		AMAZONCLIENTSECRET,
		AMAZONOPENIDSCOPE,
		AMAZONPROFILEENDPOINT,
		OUTLOOKPROFILEENDPOINT,
		DEFAULTLOGINPAGE
	}

	public enum SessionVars {
		OPENID_STATE
	}

	@Override
	public void initMod() {
		initPrefixes();
		initProperties();
		initUnrestricted();
		initGroupsAndUsers();
		initSettings();
		initPages();
		initIdentityProviders();
	}

	private void initGroupsAndUsers() {
		Resource adminGroup = groupDto.addGroup("admin", "Administrator group");
		groupDto.addGroup("private", "Private group");
		groupDto.addGroup("friends", "Friends group");
		groupDto.addGroup("family", "Family group");
		groupDto.addGroup("personal", "Personal group");
		groupDto.addGroup("identified", "Identified group");
		
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
		authRoot.addProperty(Triki.unrestricted, ".*\\.svg");
	}

	private void initPrefixes() {
		model.setNsPrefix("foaf", FOAF.NS);
		model.setNsPrefix("group", props.getPrivateUrl() + "group/");
		model.setNsPrefix("user", props.getPrivateUrl() + "user/");

		prefixDto.addPrefix("idp", props.getPrivateUrl() + "idp/");
	}
	
	private void initProperties() {
		propertyDto.addProperty("email", Foaf.mbox.getURI(), 15);
		propertyDto.addProperty("login", Triki.login.getURI(), 28);
		propertyDto.addProperty("password", Triki.password.getURI(), 29);
		propertyDto.addProperty("member", Foaf.member.getURI(), 30);
		propertyDto.addProperty("homepage", Foaf.homepage.getURI(), 16);

		propertyDto.addProperty("oauthauthendpoint", Triki.oauthauthendpoint.getURI(), 20);
		propertyDto.addProperty("oauthtokenendpoint", Triki.oauthtokenendpoint.getURI(), 22);
		propertyDto.addProperty("oauthscope", Triki.oauthscope.getURI(), 24);
		propertyDto.addProperty("oauthclientid", Triki.oauthclientid.getURI(), 26);
		propertyDto.addProperty("oauthclientsecret", Triki.oauthclientsecret.getURI(), 28);
	}

	private void initPages() {
		pageDto.addPage("login", typeDto.getType("login"), "Login", "public");
		pageDto.addPage("auth/openidlogin", typeDto.getType("auth"), "Authorise OpenID Login", "public");
		pageDto.addPage("auth/openidconnect", typeDto.getType("auth"), "Authorise OpenID Token Exchange", "public");
	}

	private void initIdentityProviders(){
		identityProviderDto.addIdentityProvider("google", "https://accounts.google.com/o/oauth2/v2/auth", "https://oauth2.googleapis.com/token", "openid email profile");
		identityProviderDto.addIdentityProvider("amazon", "https://www.amazon.com/ap/oa", "https://api.amazon.com/auth/o2/token", "profile");
		identityProviderDto.addIdentityProvider("yahoo", "https://api.login.yahoo.com/oauth2/request_auth", "https://api.login.yahoo.com/oauth2/get_token", "openid");
	}

	private void initSettings() {
		settingDto.addSetting(Settings.OPENIDCONNECTREDIRECTURI.name(), "https://www.yoursite.net/auth/openidconnect", "OpenID Connect Redirect URL");
		settingDto.addSetting(Settings.INDIELOGINROOT.name(), "https://indieauth.com/auth", "IndieLogin Login URL");
		settingDto.addSetting(Settings.INDIELOGINCLIENTID.name(), "https://www.yoursite.net/", "Indie Login client ID");
		settingDto.addSetting(Settings.INDIELOGINREDIRECTURI.name(), "https://www.yoursite.net/auth/indie", "Indie Login redirect URL");
		settingDto.addSetting(Settings.TWITTERAUTHENDPOINT.name(), "https://api.twitter.com/oauth2/token", "Twitter OAuth2 URL");
		settingDto.addSetting(Settings.TWITTERCLIENTID.name(), "Undefined","Generated Twitter OAuth2 client ID");
		settingDto.addSetting(Settings.TWITTERCLIENTSECRET.name(),"Undefined","Generated Twitter OAuth2 client secret");
		settingDto.addSetting(Settings.AMAZONAUTHENDPOINT.name(), "https://www.amazon.com/ap/oa", "Amazon OAuth2 URL");
		settingDto.addSetting(Settings.AMAZONTOKENENDPOINT.name(), "https://api.amazon.com/auth/o2/token", "Amazon Token Exchange URL");
		settingDto.addSetting(Settings.AMAZONCLIENTID.name(), "Undefined","Generated Amazon OAuth2 client ID");
		settingDto.addSetting(Settings.AMAZONCLIENTSECRET.name(),"Undefined","Generated Amazon OAuth2 client secret");
		settingDto.addSetting(Settings.AMAZONOPENIDSCOPE.name(), "profile", "Amazon OpenID scope");
		settingDto.addSetting(Settings.AMAZONPROFILEENDPOINT.name(), "https://api.amazon.com/user/profile", "Amazon profile endpoint");
		settingDto.addSetting(Settings.YAHOOAUTHENDPOINT.name(), "https://api.login.yahoo.com/oauth2/request_auth", "Yahoo OAuth2 URL");
		settingDto.addSetting(Settings.YAHOOTOKENENDPOINT.name(), "https://api.login.yahoo.com/oauth2/get_token", "Yahoo Token Exchange URL");
		settingDto.addSetting(Settings.YAHOOCLIENTID.name(), "Undefined","Generated Yahoo OAuth2 client ID");
		settingDto.addSetting(Settings.YAHOOCLIENTSECRET.name(),"Undefined","Generated Yahoo OAuth2 client secret");
		settingDto.addSetting(Settings.YAHOOOPENIDSCOPE.name(), "openid", "Yahoo OpenID scope");
		settingDto.addSetting(Settings.YAHOOPROFILEENDPOINT.name(), 'https://social.yahooapis.com/v1/user/${guid}/profile?format=json', "Yahoo profile endpoint");
		settingDto.addSetting(Settings.OUTLOOKPROFILEENDPOINT.name(), "https://outlook.office.com/api/v2.0/me", "Outlook profile endpoint");
		settingDto.addSetting(Settings.DEFAULTLOGINPAGE.name(), "/login", "Default login page");
	}

	@Override
	public void initWeb() {
		sch.addFilter(new FilterHolder(new DelegatingFilterProxy("accessFilter")), "/*", EnumSet.allOf(DispatcherType.class));
	}

}
