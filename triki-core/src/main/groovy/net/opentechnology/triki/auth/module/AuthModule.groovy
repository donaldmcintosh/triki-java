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

package net.opentechnology.triki.auth.module;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.filter.DelegatingFilterProxy;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.dto.GroupDto;
import net.opentechnology.triki.core.dto.PropertyDto
import net.opentechnology.triki.core.dto.UserDto;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.schema.Triki;

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
	private PropertyDto propertyDto;

	@Override
	public void initMod() {
		initPrefixes();
		initProperties();
		initUnrestricted();
		initGroupsAndUsers();
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
	}

	@Override
	public void initWeb() {
		sch.addFilter(new FilterHolder(new DelegatingFilterProxy("accessFilter")), "/*", EnumSet.allOf(DispatcherType.class));
	}

}
