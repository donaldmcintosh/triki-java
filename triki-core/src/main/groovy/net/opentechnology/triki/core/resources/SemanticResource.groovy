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

package net.opentechnology.triki.core.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate
import org.apache.cxf.jaxrs.ext.multipart.Attachment
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.stringtemplate.v4.ST

import com.sun.java.util.jar.pack.BandStructure.CPRefBand;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule
import net.opentechnology.triki.core.boot.Utilities;
import net.opentechnology.triki.core.dto.PropertyDto;
import net.opentechnology.triki.core.expander.ExpanderException;
import net.opentechnology.triki.core.expander.SourceExpander;
import net.opentechnology.triki.core.template.ResourceAdaptor;
import net.opentechnology.triki.core.template.TemplateException;
import net.opentechnology.triki.core.template.TemplateStore;

@Path("/")
public class SemanticResource extends RenderResource {

	private final Logger logger  = Logger.getLogger(this.getClass());
	
	@Inject
	private CachedPropertyStore propStore;

	
	@Inject @Qualifier("asModel")
    private Model asModel;
	
	@Inject
	private CamelContext camel;
	
	@Inject
	private HttpSession session;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private CoreModule coreModule;
	
	protected static final String templateProperty = "triki_template";

	@Inject
    private final Utilities utils;
	
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getRootResource(@Context HttpServletRequest req) throws ResourceException, TemplateException, ExpanderException{
		String content;
		String url =  propStore.getPrivateUrl();
		content = renderContent(url);
		callPostRenderers(url, req)

		return content;
	}
	
	@Path("{id}")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getResource(@Context HttpServletRequest req,
			@PathParam("id") String id,
			@QueryParam("action") String action,
			@QueryParam("errorMsg") String errorMsg) throws ResourceException, TemplateException, ExpanderException{
		String content;
    	String url =  propStore.getPrivateUrl() + id;
    	content = renderContent(url);
		callPostRenderers(url, req)

    	return content;
	}

	private callPostRenderers(String url, HttpServletRequest req) {
		coreModule.postRenderListeners.each { listener ->
			listener.rendered(url, req, session);
		}
	}
			
	@Path("{prefix}/{id}")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getPrefixedResource(@Context HttpServletRequest req,
			@PathParam("id") String id,
			@PathParam("prefix") String prefix) throws ResourceException, TemplateException, ExpanderException{
		String content;
		String url =  propStore.getPrivateUrl() + prefix + "/" + id;
		content = renderContent(url);
		callPostRenderers(url, req)

		return content;
	}
	
	@Path("/rss/{id}")
	@GET
	@Produces(TrikiMediaTypes.XML_UTF8)
	public String getRssResource(@Context HttpServletRequest req,
			@PathParam("id") String id,
			@QueryParam("action") String action,
			@QueryParam("errorMsg") String errorMsg) throws ResourceException, TemplateException, ExpanderException{
		String content;
    	String url = getResourceUri(req);
    	content = renderContent(url);

    	return content;
	}

	@Path("/as/{id}")
	@GET
	@Produces(TrikiMediaTypes.JSON_UTF8)
	public String getActivityStreamResource(@Context HttpServletRequest req,
			@PathParam("id") String id,
			@QueryParam("action") String action,
			@QueryParam("errorMsg") String errorMsg) throws ResourceException, TemplateException, ExpanderException{
    	
		String content = utils.writeSite(asModel, "JSON-LD");

    	return content;
	}
	
	protected String getResourceUri(HttpServletRequest req){
		return propStore.getPrivateUrl() + req.getPathInfo();
	}

	protected String renderError(String errorMsg) throws TemplateException{
		ST template = templateStore.getTemplate("error");
		Properties props = new Properties();
		props.setProperty("Uh oh, slight problem.", errorMsg);
		template.add("props", props);
		
		return template.render();
	}
	
	private String decodeProperty(String property) {
		String getUrl = siteModel.getNsPrefixURI("property");
		Resource propResource = siteModel.getResource(getUrl + property);
		return propResource.getPropertyResourceValue(DCTerms.identifier).getURI();
	}
}
