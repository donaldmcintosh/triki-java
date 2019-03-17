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

package net.opentechnology.triki.core.dto

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki
import net.opentechnology.triki.sparql.SparqlExecutor
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.beans.factory.annotation.Qualifier

import javax.inject.Inject

public class IdentityProviderDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	public void addIdentityProvider(String name, String authEndpoint, String tokenEndpoint,
									String scope)
	{
		String resName = props.getPrivateUrl() + "idp/" + name;
		Resource idp = model.createResource(resName);
		checkResource(idp, RDF.type, Triki.IdentityProvider);
		checkString(idp, DCTerms.title, name);
		checkString(idp, Triki.oauthauthendpoint, authEndpoint);
		checkString(idp, Triki.oauthtokenendpoint, tokenEndpoint);
		checkString(idp, Triki.oauthscope, scope);
		checkString(idp, Triki.oauthclientid, "undefined");
		checkString(idp, Triki.oauthclientsecret, "undefined");
	}
	
	public getIdentityProvider(String alias, Closure action)
	{
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?sub
		WHERE {  
			 ?sub a triki:IdentityProvider .
			 ?sub dc:title "${alias}" .
        }
"""
	
		sparqler.execute(model, query){ QuerySolution soln ->
			action(soln.get("sub").asResource())
		}
	}

}
