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

package net.opentechnology.triki.core.dto;

import javax.inject.Inject;

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel;
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class PropertyDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	public void addProperty(String alias, String url, int order) 
	{
		String resName = props.getPrivateUrl() + "property/" + alias;
		Resource property = model.createResource(resName);
		checkResource(property, RDF.type, Triki.Property);
		checkString(property, DCTerms.title, alias);
		Resource propertyRes = model.createResource(url);
		checkResource(property, DCTerms.identifier, propertyRes);
		Literal orderLiteral = model.createTypedLiteral(order);
		checkLiteral(property, Triki.order, orderLiteral);
	}
	
	def void getPropertyFromUrl(String url, Closure resultAction)
	{
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?title ?sub
		WHERE {  
			 ?sub dc:identifier <${url}> .  
			 ?sub a triki:Property .
			 ?sub dc:title ?title .
        }
"""
	
		sparqler.execute(model, query){ QuerySolution soln ->
			resultAction(soln.get("title").asLiteral().toString(), soln.get("sub").asResource().getURI().toString())
		}
		
	}
	
	public Property getPropertyFromType(String typeUrl)
	{
		Resource propertyType = model.getResource(typeUrl);
		Resource propAsResource = propertyType.getRequiredProperty(DCTerms.identifier).getObject()
		return model.getProperty(propAsResource.URI);
	}
	
	public void addResourceTitle(String url, String title)
	{
		Resource prefix = model.createResource(url);
		checkString(prefix, DCTerms.title, title);
	}
	
	private propertyHandling(NodeFormModel formModel) {
		if(formModel.linkProperties.any {
			LinkProperty link -> link.getPropertyField().property == RDF.type &&
			link.getValueField().link == Triki.Property })
		{
			LinkProperty identifier = formModel.linkProperties.find { LinkProperty link -> link.getPropertyField().property == DCTerms.identifier }
			if (identifier == null)
			{
				formModel.errors << "A Property must define an Identifier URL."
			}
			else if(!model.nsPrefixMap.any { 
					identifier.valueField.value.startsWith(it.value)
				})
			{
				formModel.errors << "When adding a new property, there must be a prefix for the associated identifer: ${identifier.valueField.value}"
			}
		}
	}
	
}
