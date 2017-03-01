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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import net.opentechnology.triki.core.dto.GroupDto;

public class AuthorisationManager {
	
	@Inject HttpSession session;
	
	@Inject	@Qualifier("siteModel")
    private Model model;
	
	@Inject
	private GroupDto groupDto;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	public boolean unrestricted(String url) {
		logger.debug("Checking unrestricted " + url);
		if(checkUnrestricted(url)){
			return true;
		}
		
		return false;
	}
	
	public boolean allowAccess(String url){
		if(unrestricted(url)){
			return true;
		}
		else if(publicAccess(url)){
			return true;
		}
		else if(session.getAttribute("person") != null){
			Resource person = (Resource) session.getAttribute("person");
			if(authorise(url, person)){
				return true;
			}
			else if (isAdmin(person))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAdmin(Resource person) {
		List<Resource> personGroups = getPersonGroups(person);
		Resource publicGroup = groupDto.getGroup("admin");
		for(Resource group: personGroups)
		{
			if(group.equals(publicGroup))
			{
				return true;
			}
		}
		
		return false;
	}

	public boolean publicAccess(String url)
	{
		return publicAccessStatic(url, model);
	}
	
	public boolean publicAccessStatic(String url, Model model) {
		logger.debug("Checking public access " + url);
		Resource publicGroup = groupDto.getGroup("public");
		String publicUrl = publicGroup.getURI();
		String queryString = 
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?url " +
				"WHERE {" +
				"   <" + url + "> triki:restricted <" + publicUrl + "> . " +
				" } ";

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			return true;
		}
		
		return false;
	}
	
	public boolean authorise(String url, Resource person){
		logger.info("Checking authorise for " + url + " person " + person.getURI());
		List<Resource> resourceGroups = getResourceGroups(url);
		List<Resource> personGroups = getPersonGroups(person);
		for(Resource urlGroup: resourceGroups){
			if(personGroups.contains(urlGroup)){
				return true;
			}
		}
			
		return false;
	}

	private List<Resource> getResourceGroups(String url) {
		List<Resource> groups = new ArrayList<Resource>();
		String queryString = 
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?group " +
				"WHERE {" +
				"      <" + url + "> triki:restricted ?group . " +
				" } ";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		while(results.hasNext()){
			QuerySolution soln = results.next();
			Resource group = soln.getResource("group");
			groups.add(group);
		}
		
		qe.close();
		return groups;
	}
	
	private List<Resource> getPersonGroups(Resource person) {
		List<Resource> groups = new ArrayList<Resource>();
		String queryString = 
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT ?group " +
				"WHERE {" +
				"      <" + person.getURI() + "> foaf:member ?group . " +
				"      }";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		while(results.hasNext()){
			QuerySolution soln = results.next();
			Resource group = soln.getResource("group");
			groups.add(group);
		}
		
		qe.close();
		return groups;
	}
	
	private boolean checkUnrestricted(String url){
		List<Literal> patterns = getUnrestrictedPatterns(url);
		for(Literal pattern: patterns){
			if(url.matches(pattern.getString())){
				return true;
			}
		}
		
		return false;
	}

	private List<Literal> getUnrestrictedPatterns(String url) {
		List<Literal> patterns = new ArrayList<Literal>();
		String queryString = 
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?pattern " +
				"WHERE {" +
				"  ?sub triki:unrestricted ?pattern . " +
				"  } ";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		while(results.hasNext()){
			QuerySolution soln = results.next();
			Literal pattern = soln.getLiteral("pattern");
			patterns.add(pattern);
		}
		
		return patterns;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}

	private boolean imageAuth(String url){
		logger.info("Checking image authorisation " + url);
		if(url.contains("thumb")){
			String webUrl = url.replaceFirst("thumb", "web");
			return allowAccess(webUrl);
		}
		
		String resourceUrl = url.replaceFirst("\\/image\\/", "\\/resource\\/");
		if(allowAccess(resourceUrl)){
			return true;
		}
		
		return false;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

}
