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

import javax.ws.rs.GET
import javax.inject.Inject;
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.boot.Utilities;
import net.opentechnology.triki.sparql.SparqlExecutor
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;

import org.springframework.beans.factory.annotation.Qualifier;

@Path("/search")
class SeachResource {
	
	@Inject @Qualifier("siteModel")
	protected Model siteModel;
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private Utilities utils;
	
	@GET
	@Produces("application/json")
	public List<SearchResult> search(@QueryParam("term") String match)
	{
		List<SearchResult> results = new ArrayList<>();
		
		SparqlExecutor sparqler = new SparqlExecutor();
		String queryString =	"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?sub ?title
			WHERE {  
				?sub dc:title ?title .
				FILTER ( regex (str(?title), "${match}", "i") )
            }
		""";
			
		sparqler.execute(siteModel, queryString) { QuerySolution soln ->
			SearchResult result = new SearchResult();
			result.value = soln.get("title").asLiteral().toString();
			def subGraphUrl = "/graph/subject/" + utils.encodeResource(soln.get("sub").asResource().URI);
			result.id = subGraphUrl;
			result.encodeurl = utils.encodeResource(soln.get("sub").asResource().URI);
			
			results.add(result);
		}
		
		return results;
	}
	
	@GET
	@Path("/properties")
	@Produces("application/json")
	public List<SearchResult> searchProperties(@QueryParam("term") String match)
	{
		List<SearchResult> results = new ArrayList<>();
		
		SparqlExecutor sparqler = new SparqlExecutor();
		String queryString =	"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?sub ?title
			WHERE {  
				?sub a triki:Property .
				?sub dc:title ?title .
				FILTER ( regex (str(?title), "${match}", "i") )
            }
		""";
			
		sparqler.execute(siteModel, queryString) { QuerySolution soln ->
			SearchResult result = new SearchResult();
			result.value = soln.get("title").asLiteral().toString();
			def subGraphUrl = "/graph/subject/" + utils.encodeResource(soln.get("sub").asResource().URI);
			result.id = subGraphUrl;
			result.encodeurl = utils.encodeResource(soln.get("sub").asResource().URI);
			
			results.add(result);
		}
		
		return results;
	}

	@GET
	@Path("/prefixes")
	@Produces("application/json")
	public List<SearchResult> searchPrefixes(@QueryParam("term") String match)
	{
		List<SearchResult> results = new ArrayList<>();
		
		SparqlExecutor sparqler = new SparqlExecutor();
		String queryString =	"""
		    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
			PREFIX dc:    <http://purl.org/dc/terms/>
		    SELECT ?sub ?title ?id
			WHERE {  
				?sub a triki:Prefix .
				?sub dc:title ?title .
				?sub dc:identifier ?id .
				FILTER ( regex (str(?title), "${match}", "i") && regex (str(?id), "localhost", "i") )
            }
		""";
			
		sparqler.execute(siteModel, queryString) { QuerySolution soln ->
			SearchResult result = new SearchResult();
			result.value = new URL(soln.get("id").asResource().URI).path;
			def subGraphUrl = "/graph/subject/" + utils.encodeResource(soln.get("sub").asResource().URI);
			result.id = subGraphUrl;
			result.encodeurl = utils.encodeResource(soln.get("sub").asResource().URI);
			
			results.add(result);
		}
		
		return results;
	}
}
