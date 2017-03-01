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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession;

import net.opentechnology.triki.auth.AuthorisationManager
import net.opentechnology.triki.core.boot.Utilities;
import net.opentechnology.triki.core.expander.ExpanderException;
import net.opentechnology.triki.core.expander.SourceExpander;
import net.opentechnology.triki.core.renderer.PublicURL
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor;
import net.opentechnology.triki.auth.resources.AuthenticateResource;
import net.opentechnology.triki.schema.Dcterms;

import org.apache.commons.configuration.Configuration;
import org.apache.cxf.transport.Session
import org.apache.log4j.Logger;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class ResourceAdaptor implements ModelAdaptor {
	
	private final Logger logger;
    private Properties props;
    private String url;
	private Model model;
	private final String TRIKI_DELIM = "_";
	
	@Inject
	HttpSession session;

	@Inject
	private SourceExpander expander;
	
	@Inject
    private AuthorisationManager authManager;
	
	@Inject
	private final Utilities utils;
	
    public ResourceAdaptor(Model model) throws TemplateException{
        this.logger = Logger.getLogger(this.getClass());
		this.model = model;
    }
			
	public void setUrl(String url) {
		this.url = url;
	}

	public Object getProperty(Interpreter interp, ST self, Object o,
			Object property, String propertyName)
			throws STNoSuchPropertyException {
		List<Object> values = new ArrayList<Object>();
		// Override target URL
		if(o instanceof RDFNode){
			RDFNode sub = (RDFNode) o;
			url = sub.asResource().getURI();
		} else if(o instanceof String){
			url = (String) o;
		} else {
			values.add("Unexpected template type " + o.getClass());
		}
		
		if(propertyName.equals("sessionid"))
		{
			return getSessionId()
		}
		else if(propertyName.equals("sessionhome"))
		{
			return getSessionHome()
		}
		else if (propertyName.equals("relurl")) {
			try {
				return new URL(url);
			} catch (MalformedURLException e) {
				logger.error(e.getMessage());
				return "Bad URL";
			}
		}	
		else if (propertyName.equals("puburl")) {
			try {
				return new PublicURL(url);
			} catch (MalformedURLException e) {
				logger.error(e.getMessage());
				return "Bad URL";
			}
		}
		
		values = getMultipleProperty(propertyName);
		if(values.size() == 1){
			try {
				if(translateToProperty(propertyName).equals(Triki.include)){
					values.set(0, expander.expand(values.get(0).toString()));
				}
			}
			catch(TemplateException te){
				//values.add(te.getMessage());
				logger.error(te.getMessage());
				return "";
			} catch (ExpanderException ee) {
				//values.add(ee.getMessage());
				logger.error(ee.getMessage());
				return "";
			}
			
			return values.get(0);
		}
		else {
			return values;
		}
	}

	private getSessionId() {
		if(session != null && session.getAttribute(AuthenticateResource.SESSION_ID) != null)
		{
			String id = session.getAttribute(AuthenticateResource.SESSION_ID);
			return truncateId(id)
		}
		else if(session != null && session.getAttribute(AuthenticateResource.SESSION_PERSON) != null)
		{
			Resource person = session.getAttribute(AuthenticateResource.SESSION_PERSON);
			return truncateId(person.getProperty(Dcterms.title).getString());
		}
		else {
			return null;
		}
	}
	
	private getSessionHome() {
		if(session != null && session.getAttribute(AuthenticateResource.SESSION_PERSON) != null)
		{
			Resource person = session.getAttribute(AuthenticateResource.SESSION_PERSON);
			Resource home = person.getPropertyResourceValue(Triki.home);
			if(home != null)
			{
				return utils.makeUrlPublic(home.getURI().toString());
			}
			else 
			{
				return null;
			}
		}
		else {
			return null;
		}
	}

	private truncateId(String id) {
		if(id.length() > 25)
		{
			return id.substring(0,24)
		}
		else{
			return id;
		}
	}
	
	private List<Object> getMultipleProperty(String propertyName) {
		Resource resource = model.getResource(url);
		String encodedPredicates = propertyName;
		String[] predicateList = encodedPredicates.split("/");
		List<Object> objects = new ArrayList<Object>();
		List<RDFNode> objectNodes = new ArrayList<RDFNode>();
		
		if(predicateList.length == 0 || predicateList.length > 3){
			objects.add("Property should have one or two references, not " + propertyName);
			return objects;
		}
		
		try {
			if(propertyName.startsWith("S")){
				// String off S
				objectNodes = getMultipleSubjectNode(resource, predicateList[0].substring(1, predicateList[0].length()));
			}
			else if(propertyName.equals("triki_sparql")){
				objectNodes = getSuppliedSparql(resource, predicateList[0]);
			}
			else {
				objectNodes = getMultipleObjectNode(resource, predicateList[0]);
			}
			
			for(int i=0; i<objectNodes.size(); i++){
				RDFNode objectNode = objectNodes.get(i);
				if(objectNode.isLiteral()){
					objects.add(objectNode.asLiteral().getValue());
				}
				else if(objectNode.isResource()){
					if(predicateList.length == 1){
						objects.add(objectNode);
					}
					else {
						RDFNode refObj = getObjectNode(objectNode.asResource(), predicateList[1]);
						if(refObj.isLiteral()){
							objects.add(refObj.asLiteral().getValue().toString());
						}
						else {
							objects.add(refObj.asResource().getURI());
						}
					}
				}
				else {
					objects.add(objectNode + " was of unexpected type.");
				}
			}
		}
		catch (TemplateException re){
			//objects.add("Error: " + re.getMessage());
			logger.error(re.getMessage());
		}
		
		return objects;
	}
	
	private List<RDFNode> getMultipleSubjectNode(Resource resource, String encodedPredicate) throws TemplateException {
		List<RDFNode> subjects = new ArrayList<RDFNode>();
		Property predicate = translateToProperty(encodedPredicate);
		SparqlExecutor sparqler = new SparqlExecutor();
		
		String queryString =
		"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?sub  
			 WHERE {  
			 ?sub <$predicate.URI> <$resource.URI> .  
			 ?sub <http://purl.org/dc/terms/created> ?created .
	            OPTIONAL { 
		             <$resource.URI> triki:order ?order .  
		             ?sub ?order ?seq .
	             }  
             }
			ORDER BY ASC(?seq) ASC(?created) ?sub
		""";
		
		logger.info(queryString)
		sparqler.execute(model, queryString) { QuerySolution soln ->
			RDFNode sub = soln.get("sub");
			if(sub.isLiteral()){
				subjects.add(sub);
			}
			else if (sub.isResource()){
				Resource subRes = sub.asResource();
				if(authManager.allowAccess(subRes.getURI().toString())){
					subjects.add(subRes);
				}
				// else not authorized to see this
			}
		}
		
		return subjects;
	}

	private RDFNode getObjectNode(Resource resource, String encodedPredicate) throws TemplateException  {
		Property predicate = translateToProperty(encodedPredicate);
		Statement anyMatch = resource.getProperty(predicate);
		if(anyMatch != null){
			RDFNode objectNode = anyMatch.getObject();
			return objectNode;
		}
		else {
			throw new TemplateException("Resource " + resource.getURI() + " does not have property " + predicate.getURI());
		}
	}
	
	private List<RDFNode> getMultipleObjectNode(Resource resource, String encodedPredicate) throws TemplateException {
		List<RDFNode> objects = new ArrayList<RDFNode>();
		Property predicate = translateToProperty(encodedPredicate);
		SparqlExecutor sparqler = new SparqlExecutor();

		String queryString =
		"""
			PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
			SELECT ?obj 
			 WHERE {  
			<$resource.URI> <$predicate.URI> ?obj . 
			OPTIONAL { 
				 ?obj triki:order ?order .  
				 ?obj ?order ?seq .
				 }  
			 } ORDER BY ASC(?seq) ?obj 
		""";
		
		logger.debug("Sparql: " + queryString);
		
		sparqler.execute(model, queryString) { QuerySolution soln ->
			RDFNode obj = soln.get("obj");
			if(obj.isLiteral()){
				objects.add(obj);
			}
			else if (obj.isResource()){
				Resource objRes = obj.asResource();
				if(authManager.allowAccess(objRes.getURI().toString())){
					objects.add(objRes);
				}
				// else not authorized to see this
			}
		}
		
		if(objects.size() > 0){
			return objects;
		}
		else {
			throw new TemplateException("Resource " + resource.getURI() + " does not have property " + predicate.getURI());
		}
	}
	
	private List<RDFNode> getSuppliedSparql(Resource resource, String encodedPredicate) throws TemplateException {
		List<RDFNode> objects = new ArrayList<RDFNode>();
		RDFNode sparqlNode = getObjectNode(resource, encodedPredicate);
		SparqlExecutor sparqler = new SparqlExecutor();
		String sparql= "";
		if(sparqlNode.isLiteral()){
			sparql = sparqlNode.asLiteral().getString();
		}
		else {
			throw new TemplateException("triki:sparql resource should be a literal.");
		}
		
		sparqler.execute(model, sparql) { QuerySolution soln ->
			RDFNode target = soln.get("target");
			if(target.isLiteral()){
				objects.add(target);
			}
			else if (target.isResource()){
				Resource objRes = target.asResource();
				if(authManager.allowAccess(objRes.getURI().toString())){
					objects.add(objRes);
				}
				// else not authorized to see this
			}
		}
		
		if(objects.size() > 0){
			return objects;
		}
		else {
			throw new TemplateException("Resource " + resource.getURI() + " does not have property " + encodedPredicate);
		}
	}

	private Property translateToProperty(String encodedPredicate) throws TemplateException {
		String nsPrefix = encodedPredicate.replaceAll("${TRIKI_DELIM}.*\$", "");
		String property = encodedPredicate.replaceAll("^.*${TRIKI_DELIM}", "");
		if(nsPrefix == null || property == null){
			throw new TemplateException("Bad encoded predicate " + encodedPredicate);
		}
		else {
			if(nsPrefix.startsWith("S")){
				nsPrefix = nsPrefix.substring(1, nsPrefix.length());
			}
			
			String fullPrefixUri = model.getNsPrefixURI(nsPrefix);
			if(fullPrefixUri == null){
				throw new TemplateException("No namespace prefix for " + nsPrefix);
			}
			String fullUrl = fullPrefixUri + property;
			try {
				new URL(fullUrl);
			} catch (MalformedURLException e) {
				throw new TemplateException("Bad predicate URL " + fullUrl, e);
			}
			return model.getProperty(fullUrl);
		}
	}
	
	
	public void setModel(Model model) {
		this.model = model;
	}

	public void setExpander(SourceExpander expander) {
		this.expander = expander;
	}

	public void setAuthManager(AuthorisationManager authManager) {
		this.authManager = authManager;
	}

}
