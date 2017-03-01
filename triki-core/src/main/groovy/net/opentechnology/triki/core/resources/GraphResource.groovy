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
import java.util.Properties
import java.util.concurrent.ConcurrentSkipListMap.Iter;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse;
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
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.jaxrs.ext.multipart.Attachment
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody
import org.apache.cxf.jaxrs.provider.RequestDispatcherProvider
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.shared.PropertyNotFoundException
import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.stringtemplate.v4.ST;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities;
import net.opentechnology.triki.core.dto.MediaTypeDto
import net.opentechnology.triki.core.dto.PrefixDto
import net.opentechnology.triki.core.dto.PropertyDto;
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.expander.ExpanderException;
import net.opentechnology.triki.core.expander.SourceExpander
import net.opentechnology.triki.core.model.ModelException
import net.opentechnology.triki.core.renderer.DateRenderer;
import net.opentechnology.triki.core.template.ResourceAdaptor;
import net.opentechnology.triki.core.template.TemplateException;
import net.opentechnology.triki.core.template.TemplateStore
import net.opentechnology.triki.schema.Dcterms
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

@Path("/graph")
public class GraphResource extends RenderResource {
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private CamelContext camel;
	
	@Inject
	private HttpSession session;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private PrefixDto prefixDto;
	
	@Inject
	private ResourceDto resourceDto;
	
	@Context
	private ServletContext sc;
	
	@Inject
	private MediaTypeDto mediaTypeDto;
	
	protected static final String templateProperty = "triki_template"
	
	@Inject
	private ApplicationContext ctx;

    //private final AuthenticationManager authMgr;
	@Inject
    private Utilities utils;

	protected String getResourceUri(HttpServletRequest req){
		return propStore.getPrivateUrl() + req.getPathInfo();
	}
	
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getGraph() throws ResourceException, TemplateException, ExpanderException{
		String url =  propStore.getPrivateUrl() + "graph";
		String content = renderContent(url);
		
		return content
	}

	@Path("addnode")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String addNode(@Context HttpServletRequest req) throws ResourceException, TemplateException, ExpanderException{
		String url = propStore.getPrivateUrl() + "addnode";
		ST template = templateStore.getTemplate("addnode");
		
		// May be set if validation errors
		NodeFormModel formModel = req.getAttribute("resp")
		if(formModel == null){
			formModel = ctx.getBean(NodeFormModel.class)
			formModel.action = NodeFormModel.Action.add
		}
		
		template.add("model", formModel)
		
		String rendered = template.render();
		
		return rendered
	}

	@Path("subject/{id}")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getGraphForSubject(@PathParam("id") String encodedId, @QueryParam("action") String action, @Context HttpServletRequest req) throws ResourceException, TemplateException, ExpanderException{

		if(action == null || action == NodeFormModel.Action.view.name())
		{
			return getSubjectGraph(encodedId, req);
		}
		else if(action == NodeFormModel.Action.edit.name())
		{
			NodeFormModel formModel = ctx.getBean(NodeFormModel.class)
			formModel.getFormData(encodedId, NodeFormModel.Action.edit)
			
			ST template = templateStore.getTemplate("addnode");
			template.add("model", formModel);
			session.setAttribute("origUrl", formModel.nodeAddress.url)

			return template.render();
		}
		else if(action == NodeFormModel.Action.clone.name())
		{
			NodeFormModel formModel = ctx.getBean(NodeFormModel.class)
			formModel.getFormData(encodedId, NodeFormModel.Action.clone)
			formModel.removeCreatedCreator();
			
			ST template = templateStore.getTemplate("addnode");
			template.add("model", formModel);
			session.setAttribute("origUrl", formModel.nodeAddress.url)

			return template.render();
		}
		else if(action == NodeFormModel.Action.delete.name())
		{
			List msgs = []
			String resourceUrl = utils.decodeShortResource(encodedId)
			resourceDto.deleteResource(resourceUrl, msgs)
			
			ST template = templateStore.getTemplate("conf");
			template.add("props", resourceUrl)
			template.add("msgs", msgs);
			String output = template.render();

			return template.render();
		}
		else
		{
			return "meh"
		}
	}
			
	@Path("response")
	@POST
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getGraphForResponse(@Context HttpServletRequest req) throws ResourceException, TemplateException, ExpanderException{
		NodeFormModel formModel = req.getAttribute("resp")
		if(formModel.msgs && formModel.errors.size() == 0)
		{
			return getGraphForSubject(utils.encodeResource(formModel.nodeAddress.url), NodeFormModel.Action.view.name(), req)
		}
		else
		{
			return addNode(req);
		}
	}
			
	@Path("object/{id}")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getGraphForObject(@PathParam("id") String id,
			@QueryParam("action") String action,
			@QueryParam("errorMsg") String errorMsg) throws ResourceException, TemplateException, ExpanderException{
		String resourceUrl = utils.decodeShortResource(id);
		List<Map<String, String>> graphTable = new ArrayList<>();
		String rendered = getObjectGraph(id, graphTable, resourceUrl);
		return rendered;
	}
	
	@Path("/{type}/{instance}")
	@GET
	@Produces(TrikiMediaTypes.HTML_UTF8)
	public String getGraphTypeInstance(@PathParam("type") String type, @PathParam("instance") String instance) throws ResourceException, TemplateException, ExpanderException{
	
		List<Map<String, String>> graphTable = new ArrayList<>();
		String url = propStore.getPrivateUrl() + type + "/" + instance;
		String rendered = getSubjectGraph(type + "/" + instance, graphTable, url);
		return rendered;
	}
			
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public NodeFormModel saveResource(MultipartBody body, @Context HttpServletResponse resp,
				@Context HttpServletRequest req) throws IOException {		
		def formData = extractFormData(body)
		NodeFormModel formModel = ctx.getBean(NodeFormModel.class)
		formModel.addFormData(formData)
		
		configTypeHandling(formModel)
		createdHandling(formModel)
		
		if(formModel.errors.size() == 0 && formModel.action == NodeFormModel.Action.edit)
		{
			// Use origUrl because there might have been a rename
			String origUrl = session.getAttribute("origUrl")
			if(origUrl != null)
			{
				Resource origRes = siteModel.createResource(origUrl);
				List<Statement> stmts = origRes.listProperties().toList();
				siteModel.remove(stmts)
			}
			else
			{
				formModel.errors << "Unrecognised session, please refesh page and try again."
			}
		}
		
		if(formModel.errors.size() == 0)
		{
			Resource res = siteModel.createResource(formModel.nodeAddress.url);

			formModel.saveTextParams(res)
			formModel.saveLinkParams(res)	
			if(formModel.action == NodeFormModel.Action.add)
			{
				resourceDto.addCreatedNowAndCreator(session, res)
				formModel.msgs << "Added node ${formModel.nodeAddress.path} successfully."
			}
			else if(formModel.action == NodeFormModel.Action.edit)
			{
				formModel.msgs << "Updated node ${formModel.nodeAddress.path} successfully."
			}
			else if(formModel.action == NodeFormModel.Action.clone)
			{
				resourceDto.addCreatedNowAndCreator(session, res)
				formModel.msgs << "Cloned node successfully."
			}
		}
		
		return formModel;
	}
				
	private configTypeHandling(NodeFormModel formModel) {
		prefixDto.prefixHandling(formModel)
		propertyDto.propertyHandling(formModel)
		mediaTypeDto.mediaTypeHandling(formModel)
	}
	
	private createdHandling(NodeFormModel formModel) {
		resourceDto.createdHandling(formModel)
	}

	private Map extractFormData(MultipartBody body) {
		def formData = [:]
		for(Attachment attachment: body.getAllAttachments() )
		{
			ContentDisposition content = attachment.getContentDisposition();
			def id = content.getParameter("name")
			def type = content.getType();
			if(type == "form-data"){
				formData."${id}" = IOUtils.toString(attachment.getDataHandler().getInputStream(), "UTF-8");
			}
			else
			{
				String filename = content.getFilename();
				//InputStream in = attachment.getObject(InputStream.class);
				//FileUtils.copyInputStreamToFile(in, new File("c:/temp/uploaded.JPG"));
				//System.out.println("Received " + name + " " + type + " " + filename);
			}
		}
		return formData
	}
				

	private String getSubjectGraph(String id, HttpServletRequest req) throws TemplateException {
		String url = utils.decodeShortResource(id);
		Resource res = siteModel.getResource(url);
		SparqlExecutor sparqler = new SparqlExecutor();
		String queryString =	"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?pred ?obj
			 WHERE {  
					<${url}> ?pred ?obj . 
					?predtype dc:identifier ?pred .
					?predtype triki:order ?order .
             }
			 ORDER BY ASC (?order)
		""";
		List<Map<String, String>> graphTable = new ArrayList<>();
		sparqler.execute(siteModel, queryString) { QuerySolution soln ->
			HashMap<String, String> graphRow = new HashMap<>();
			Resource pred = soln.get("pred");
			propertyDto.getPropertyFromUrl(pred.getURI()) { String title, String localUrl ->
				graphRow.put("pred_title", title);
				graphRow.put("pred_url_sub", getSubjectLink(localUrl));
				graphRow.put("pred_url_obj", getObjectLink(localUrl));
			}
			
			RDFNode obj = soln.get("obj");
			if(obj.isLiteral()){
				// If leaf
				graphRow.put("obj_title", obj.asLiteral().getValue());
			}
			else
			{
				if(obj.asResource().hasProperty(DCTerms.title))
				{
					String objectTitle = obj.asResource().getProperty(DCTerms.title).getString();
					graphRow.put("obj_title", objectTitle);
					obj.asResource().getURI();
					graphRow.put("obj_url_sub", getSubjectLink(obj.asResource().getURI()));
					graphRow.put("obj_url_obj", getObjectLink(obj.asResource().getURI()));
				}
				else 
				{
					graphRow.put("obj_title", obj.asResource());
					graphRow.put("obj_url_sub", getSubjectLink(obj.asResource().getURI()));
					graphRow.put("obj_url_obj", getObjectLink(obj.asResource().getURI()));
				}
			}
			graphTable.add(graphRow);
		}
		
		
		ST template = templateStore.getTemplate("subject");
		template.add("graphTable", graphTable);
		template.add("encodedId", id);
		template.add("path", new URL(url).path);
	
		(new URL(url).path.startsWith("/content")) ? template.add("isContent", "true") : false
		(new URL(url).path.startsWith("/content") && mediaTypeDto.isTextMediaType(url)) ? template.add("isTextContent", "true") : false

		// Only populated if contains msgs/errors
		NodeFormModel model = req.getAttribute("resp")
		(model) ? template.add("model", model) : false
		
		String rendered = template.render();
		return rendered;
	}
	
	private String getObjectGraph(String id, List<Map<String, String>> graphTable, String url) throws TemplateException {
		Resource res = siteModel.getResource(url);		
		SparqlExecutor sparqler = new SparqlExecutor();
		String queryString =	"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?sub ?pred
			 WHERE {  
					?sub ?pred <${url}> . 
					?predtype dc:identifier ?pred .
					?predtype triki:order ?order .
             }
			 ORDER BY ASC (?order)
		""";
			
		sparqler.execute(siteModel, queryString) { QuerySolution soln ->
			HashMap<String, String> graphRow = new HashMap<>();
			Resource pred = soln.get("pred");
			propertyDto.getPropertyFromUrl(pred.getURI()) { String title, String localUrl ->
				graphRow.put("pred_title", title);
				graphRow.put("pred_url_sub", getSubjectLink(localUrl));
				graphRow.put("pred_url_obj", getObjectLink(localUrl));
			}
			
			RDFNode sub = soln.get("sub");
			if(sub.asResource().hasProperty(DCTerms.title))
			{
				String subjectTitle = sub.asResource().getProperty(DCTerms.title).getString();
				graphRow.put("sub_title", subjectTitle);
				graphRow.put("sub_url_sub", getSubjectLink(sub.asResource().getURI()));
				graphRow.put("sub_url_obj", getObjectLink(sub.asResource().getURI()));
			}
			else
			{
				if(sub.asResource().getLocalName() != null && !sub.asResource().getLocalName().empty )
				{
					graphRow.put("sub_title", sub.asResource().getLocalName());
					graphRow.put("sub_url_sub", getSubjectLink(sub.asResource().getURI()));
					graphRow.put("sub_url_obj", getObjectLink(sub.asResource().getURI()));
				}
				else
				{
					graphRow.put("sub_title", getSubjectLink(sub.asResource().getURI().toString()));
				}
			}
			graphTable.add(graphRow);
		}
		
		ST template = templateStore.getTemplate("object");
		template.add("graphTable", graphTable);
		template.add("id", utils.decodeShortResource(id));
		
		String rendered = template.render();
		return rendered;
	}
	
	def String getSubjectLink(String url)
	{
		return "/graph/subject/" + utils.encodeResource(url);
	}
	
	def String getObjectLink(String url)
	{
		return "/graph/object/" + utils.encodeResource(url);
	}
	
}
