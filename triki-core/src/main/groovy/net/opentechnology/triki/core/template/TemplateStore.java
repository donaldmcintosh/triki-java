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

package net.opentechnology.triki.core.template;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.renderer.DateRenderer;
import net.opentechnology.triki.core.renderer.PublicURL;
import net.opentechnology.triki.core.renderer.PublicUrlRenderer;
import net.opentechnology.triki.core.renderer.RelativeUrlRenderer;
import net.opentechnology.triki.core.resources.ContentUtils;

public class TemplateStore {
	
	public static final String CORE_TEMPLATE = "core.stg";
	public static final String SITE_TEMPLATE = "site.stg";
	private final Logger logger = Logger.getLogger(this.getClass());
    private STGroup coreTemplate;
    private STGroup siteTemplate;
    protected final char d = '$';
    
    private List<STGroup> moduleTemplates = new ArrayList<>();
    
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private ContentUtils contentUtils;
	
	public void initCoreTemplate() throws TemplateException{
		coreTemplate = initTemplate(CORE_TEMPLATE);
	}
	
	public void initSiteTemplate() throws TemplateException{
		siteTemplate = initTemplate(SITE_TEMPLATE);
	}
	
	private STGroup initTemplate(String templateName) throws TemplateException {
		STGroup templateGroup;
		logger.info("Initialising template " + templateName);
		try {
			String templateStr = contentUtils.getTemplateString(templateName);
			templateGroup = new STGroupString(templateName, templateStr, d, d);
			templateGroup.registerRenderer(XSDDateTime.class, new DateRenderer());
			templateGroup.registerRenderer(URL.class, new RelativeUrlRenderer(propStore.getPrivateUrl()));
			templateGroup.registerRenderer(PublicURL.class, new PublicUrlRenderer(propStore.getPrivateUrl(), propStore.getProperty("public_url")));
			return templateGroup;
		} catch (Exception e) {
			throw new TemplateException("Bad template url " + e.getMessage());
		}
	}
	
	public ST getTemplate(String templateName) throws TemplateException{	
		ST template = coreTemplate.getInstanceOf(templateName);
		if(template == null){
			template = siteTemplate.getInstanceOf(templateName);
			if(template == null)
			{
				throw new TemplateException("No such template " + templateName);
			}
		}

		return template;

	}
	
	public void registerAdaptor(ModelAdaptor r) throws TemplateException{
		siteTemplate.registerModelAdaptor(String.class, r);
		siteTemplate.registerModelAdaptor(RDFNode.class, r);
	}

}
