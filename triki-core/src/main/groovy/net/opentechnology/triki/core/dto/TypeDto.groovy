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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class TypeDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	public Resource addType(String name, String title) 
	{
		String resName = props.getPrivateUrl() + "type/" + name;
		Resource type = model.createResource(resName);
		checkResource(type, RDF.type, Triki.Type);
		checkString(type, DCTerms.title, title);
		return type
	}
	
	public void addTypeRestricted(String name, String title, Resource group)
	{
		Resource type = addType(name, title);
		checkResource(type, Triki.restricted, group);
	}
	
	public Resource getType(String name)
	{
		String resName = props.getPrivateUrl() + "type/" + name;
		return model.createResource(resName);
	}

	public void addResourceTitle(String url, String title)
	{
		Resource prefix = model.createResource(url);
		checkString(prefix, DCTerms.title, title);
	}
}
