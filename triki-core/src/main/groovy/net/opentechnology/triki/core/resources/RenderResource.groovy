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

package net.opentechnology.triki.core.resources

import net.opentechnology.triki.core.expander.ExpanderException
import net.opentechnology.triki.core.template.ResourceAdaptor;
import net.opentechnology.triki.core.template.TemplateException
import net.opentechnology.triki.core.template.TemplateStore
import net.opentechnology.triki.modules.Module
import net.opentechnology.triki.schema.Triki;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext;
import org.stringtemplate.v4.ST

import org.apache.jena.vocabulary.RDF

import javax.inject.Inject

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.log4j.Logger;

abstract class RenderResource {
	@Inject @Qualifier("resourceAdaptor")
	protected ResourceAdaptor resourceAdaptor;
	
	@Inject @Qualifier("siteModel")
	protected Model siteModel;
	
	@Inject
	protected final TemplateStore templateStore;

	@Inject
	private ApplicationContext appCtx;
	
	private final Logger logger = Logger.getLogger(this.getClass());

	protected String renderContent(String url) throws TemplateException, ExpanderException {
		try {
			Resource resource;
			String templateName = "Unknown template";
			logger.info("Getting resource " + url);
			
			resourceAdaptor.setUrl(url);
			templateStore.registerAdaptor(resourceAdaptor);
			resource = siteModel.getResource(url);
			if(resource == null) throw new TemplateException("Could not find resource " + url);
			
			logger.info("Getting type for resource " + url);
			Statement stmt = resource.getProperty(RDF.type);
			if(stmt == null) throw new TemplateException("No type found for resource " + url);
					 
			Resource type = stmt.getObject().asResource();
			// if a page, go by name.  If not, go by type.
			templateName = type.getLocalName().toLowerCase();
			
			logger.info("Getting template for templateName " + url);
			ST template = templateStore.getTemplate(templateName);
			if(template == null) throw new TemplateException("No template found for type " + type);
			
			template.add("props", url);

			if(type.hasProperty(Triki.allowtemplateobjects)) {
				logger.info("Template ${templateName} is allowed to have objects added.");
				String[] beanNames = appCtx.getParent().getBeanNamesForType(Module.class);
				for (String beanName : beanNames) {
					Module module = appCtx.getBean(beanName, Module.class);
					String rootUrl = url.replaceAll("#.*", "")
					logger.info("Adding beans for ${beanName} for url ${rootUrl}");
					module.addTemplateObjects(template, rootUrl);
				}
			}
		
			logger.info("Rendering resource " + url);
			return template.render();
		}
		catch(Exception e)
		{
			def errors = []
			ST template = templateStore.getTemplate("error");
			errors << e.getMessage();
			template.add("errors", errors);
			return template.render();
		}
	}

}
