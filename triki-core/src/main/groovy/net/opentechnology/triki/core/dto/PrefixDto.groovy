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

import org.apache.jena.rdf.model.Literal;

import javax.inject.Inject;

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier;

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel;
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class PrefixDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	public void addPrefix(String alias, String url) 
	{
		String resName = props.getPrivateUrl() + "prefix/" + alias;
		model.setNsPrefix(alias, url);
		Resource prefix = model.createResource(resName);
		checkResource(prefix, RDF.type, Triki.Prefix);
		checkString(prefix, DCTerms.title, alias);
		Resource prefixRes = model.createResource(url);
		checkResource(prefix, DCTerms.identifier, prefixRes);
	}
	
	public getPrefixFromCode(String code, Closure action)
	{
		String prefixUrl = model.getNsPrefixURI(code);
		Resource prefix = model.createResource(prefixUrl);
		
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?title ?sub
		WHERE {  
			 ?sub dc:identifier <${prefixUrl}> .  
			 ?sub a triki:Prefix .
			 ?sub dc:title ?title .
        }
"""
	
		sparqler.execute(model, query){ QuerySolution soln ->
			action(soln.get("title").asLiteral().toString(), soln.get("sub").asResource())
		}
	}
	
	public void addResourceTitle(String url, String title)
	{
		Resource prefix = model.createResource(url);
		checkString(prefix, DCTerms.title, title);
	}
	
	private prefixHandling(NodeFormModel formModel) {
		if(formModel.linkProperties.any {
			LinkProperty link -> link.getPropertyField().property == RDF.type &&
			link.getValueField().link == Triki.Prefix })
		{
			def prefixName = formModel.nodeAddress.id
			LinkProperty identifier = formModel.linkProperties.find { LinkProperty link -> link.getPropertyField().property == DCTerms.identifier }
			TextProperty title = formModel.textProperties.find { TextProperty text -> text.getPropertyField().property == DCTerms.title }
			if(!(title.valueField.text ==~ /\w+/))
			{
				formModel.errors << "When adding a new prefix, title must be a single word."
			}
			else if (identifier == null)
			{
				formModel.errors << "A prefix must define an Identifier URL."
			}
			else
			{
				model.setNsPrefix(prefixName, identifier.valueField.value);
				formModel.msgs << "Added new graph namespace prefix ${prefixName} for URL ${identifier.valueField.value} successfully."
			}
		}
	}

}
