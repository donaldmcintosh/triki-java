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

package net.opentechnology.triki.core.boot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import net.opentechnology.triki.core.async.LiveModelSaver;
import net.opentechnology.triki.core.dto.ContentDto;
import net.opentechnology.triki.core.dto.GroupDto;
import net.opentechnology.triki.core.dto.MediaTypeDto;
import net.opentechnology.triki.core.dto.PageDto;
import net.opentechnology.triki.core.dto.PrefixDto;
import net.opentechnology.triki.core.dto.PropertyDto;
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.core.dto.SettingDto.Settings;
import net.opentechnology.triki.core.dto.TypeDto;
import net.opentechnology.triki.core.model.ModelStore;
import net.opentechnology.triki.core.template.StringTemplateValidator;
import net.opentechnology.triki.core.template.TemplateException;
import net.opentechnology.triki.core.template.TemplateStore;
import net.opentechnology.triki.modules.ContentSaver;
import net.opentechnology.triki.modules.ContentValidator;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.modules.PostRenderListener;
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;

import static net.opentechnology.triki.core.template.TemplateStore.CORE_TEMPLATE;
import static net.opentechnology.triki.core.template.TemplateStore.SITE_TEMPLATE;

public class CoreModule implements Module {
	
	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private ModelStore modelStore;
	
	@Inject
	private PrefixDto prefixDto;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private PageDto pageDto;
	
	@Inject
	private MediaTypeDto mediaTypeDto;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private ContentDto contentDto;
	
	@Inject
	private GroupDto groupDto;
	
	@Inject
	private TemplateStore templateStore;
	
	private List<Module> modules =  new ArrayList<Module>();
	private Map<String, ContentValidator> contentValidators = new HashMap<>();
	private Map<String, ContentSaver> contentSavers = new HashMap<>();
	private List<PostRenderListener> postRenderListeners = new ArrayList<>();

	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private LiveModelSaver modelSave;

	@Override
	public void initMod() throws StartupException {
		initTripleStore();
		initMediaTypes();
		initNamespacePrefixes();
		initProperties();
		initTypes();
		initPages();
		initSettings();
		initContent();
		
		registerValidator("stg", new StringTemplateValidator());
		
		initTemplates();
		
		for(Module module: modules)
		{
			module.initMod();
		}
		
		initAsync();
	}
	
	public void registerModules(Module module)
	{
		modules.add(module);
	}
	
	public void registerValidator(String suffix, ContentValidator validator)
	{
		contentValidators.put(suffix, validator);
	}
	
	public void registerPostRenderListener(PostRenderListener postRenderListen)
	{
		postRenderListeners.add(postRenderListen);
	}
	
	public void registerContentSaver(String suffix, ContentSaver saver)
	{
		contentSavers.put(suffix, saver);
	}

	private void initTemplates() throws StartupException {
		try {
			templateStore.addTemplate(SITE_TEMPLATE);
			templateStore.addTemplate(CORE_TEMPLATE);
		} catch (TemplateException e) {
			throw new StartupException(e);
		}
	}

	private void initTripleStore() throws StartupException {
		modelStore.initTripleStores();
	}
	
	private void initProperties()
	{
		typeDto.addResourceTitle(Triki.Property.getURI(), "Triki Property");
		propertyDto.addProperty("type", RDF.type.getURI(), 10);
		
		propertyDto.addProperty("title", DCTerms.title.getURI(), 11);
		propertyDto.addProperty("created", DCTerms.created.getURI(), 16);
		propertyDto.addProperty("description", DCTerms.description.getURI(), 12);
		propertyDto.addProperty("creator", DCTerms.creator.getURI(), 17);
		propertyDto.addProperty("identifier", DCTerms.identifier.getURI(), 13);
		propertyDto.addProperty("references", DCTerms.references.getURI(), 14);
		
		propertyDto.addProperty("content", Triki.content.getURI(), 19);
		propertyDto.addProperty("restricted", Triki.restricted.getURI(), 14);
		propertyDto.addProperty("include", Triki.include.getURI(), 16);
		propertyDto.addProperty("format", DCTerms.format.getURI(), 15);
		propertyDto.addProperty("order", Triki.order.getURI(), 30);
		propertyDto.addProperty("sparql", Triki.sparql.getURI(), 30);
		propertyDto.addProperty("month", Time.month.getURI(), 40);
		
		propertyDto.addProperty("setting", Triki.setting.getURI(), 20);
		propertyDto.addProperty("webcontent", Triki.webcontent.getURI(), 20);
	}
	
	private void initSettings()
	{
		settingDto.addSetting(Settings.RESTRICTION.name(), "group/private");
		settingDto.addSetting(Settings.CREATOR.name(), "user/administrator");
		settingDto.addSetting(Settings.YEARMONTHRESTRICTION.name(), "group/public");
		settingDto.addSetting(Settings.LIVEGRAPHSAVEPERIODMINS.name(), "5");
	}
	
	private void initTypes() {
		Resource publicGroup = groupDto.addGroup("public", "Public access group");
		typeDto.addResourceTitle(Triki.Type.getURI(), "Triki Type");
		typeDto.addType("conf", "Configuration");
		typeDto.addType("add", "Add Node");
		typeDto.addType("graph", "Graph");
		typeDto.addType("template", "Template");
		typeDto.addTypeRestricted("login", "Login", publicGroup);
		typeDto.addType("auth", "Auth");
		typeDto.addType("query", "Query");
		typeDto.addType("sitelogin", "Site login");
		
		typeDto.addType("page", "Standard");
		typeDto.addTypeRestricted("home", "Home", publicGroup);
		typeDto.addTypeRestricted("blog", "Blog ", publicGroup);
		typeDto.addTypeRestricted("blogs", "Blogs", publicGroup);
	}
	
	private void initMediaTypes() {
		typeDto.addResourceTitle(Triki.MediaType.getURI(), "Triki Media Type");
		mediaTypeDto.addMediaType(MediaType.TEXT_PLAIN, "ttl", "10");
		mediaTypeDto.addMediaType("text/css", "css", "10");
		mediaTypeDto.addMediaType("application/pdf", "pdf", "10");
		mediaTypeDto.addMediaType("image/png", "png", "86400");
		mediaTypeDto.addMediaType("image/gif", "gif", "10");
		mediaTypeDto.addMediaType("image/jpg", "jpg", "10");
		mediaTypeDto.addMediaType(MediaType.TEXT_PLAIN, "groovy", "10");
		mediaTypeDto.addMediaType("text/x-markdown", "md", "0");
		mediaTypeDto.addMediaType(MediaType.TEXT_PLAIN, "stg", "10");
		mediaTypeDto.addMediaType(MediaType.TEXT_PLAIN, "js", "86400");
		mediaTypeDto.addMediaType(MediaType.TEXT_PLAIN, "log", "10");
		mediaTypeDto.addMediaType("image/svg+xml", "svg", "10");
		mediaTypeDto.addMediaType("font/woff","woff", "10");
	}

	private void initNamespacePrefixes() {
		typeDto.addResourceTitle(Triki.Prefix.getURI(), "Triki Prefix");
		prefixDto.addPrefix("root", props.getPrivateUrl());
		prefixDto.addPrefix("prefix", props.getPrivateUrl() + "prefix/");
		prefixDto.addPrefix("property", props.getPrivateUrl() + "property/");
		prefixDto.addPrefix("setting", props.getPrivateUrl() + "setting/");
		prefixDto.addPrefix("type", props.getPrivateUrl() + "type/");
		prefixDto.addPrefix("content", props.getPrivateUrl() + "content/");
		prefixDto.addPrefix("mediatype", props.getPrivateUrl() + "mediatype/");
		prefixDto.addPrefix("dc", DCTerms.NS);
		prefixDto.addPrefix("triki", Triki.NS);
		prefixDto.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixDto.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixDto.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixDto.addPrefix("time", Time.NS);
		prefixDto.addPrefix("group", props.getPrivateUrl() + "group/");
		prefixDto.addPrefix("user", props.getPrivateUrl() + "user/");
		prefixDto.addPrefix("publish", props.getPrivateUrl() + "/publish/");
		prefixDto.addPrefix("blog", props.getPrivateUrl() + "blog/");
		prefixDto.addPrefix("auth", props.getPrivateUrl() + "auth/");
		prefixDto.addPrefix("query", props.getPrivateUrl() + "query/");
		prefixDto.addPrefix("localprop", props.getPrivateUrl() + "localprop/");
		prefixDto.addPrefix("time", "http://www.w3.org/2006/time#");
		prefixDto.addPrefix("year", props.getPrivateUrl() + "year/");
		prefixDto.addPrefix("month", props.getPrivateUrl() + "month/");
	}

	private void initPages() {
		pageDto.addPage("", typeDto.getType("home"), "Home Page", "public");
		pageDto.addPage("graph", typeDto.getType("graph"), "Graph Explorer", "admin");
		pageDto.addPage("template", typeDto.getType("template"), "Template Editor", "admin");
		pageDto.addPage("conf", typeDto.getType("conf"), "Configuration", "admin");
		pageDto.addPage("login", typeDto.getType("login"), "Console login", "public");
		pageDto.addPage("auth", typeDto.getType("auth"), "Authorise", "public");
		pageDto.addIndexPage("blogs", typeDto.getType("blogs"), "Blogs Index", "public", typeDto.getType("blog"));
	}
	
	public void initContent(){
		contentDto.checkFile("site.stg", "default.stg");
		contentDto.checkFile("site.css", "default.css");
		contentDto.addContent("triki.ttl");
		contentDto.addContent("triki.log");
		contentDto.addContent("triki-lb.log");
	}

	@Override
	public void initWeb() {
		// nothing to do here
	}
	
	public void initAsync() throws StartupException {
		try {
			camelCtx.addRoutes(modelSave);
		} catch (Exception e) {
			throw new StartupException(e);
		}
	}

	public Map<String, ContentValidator> getContentValidators() {
		return contentValidators;
	}
	
	
	public List<PostRenderListener> getPostRenderListeners() {
		return postRenderListeners;
	}

}
