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

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.schema.Foaf;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class UserDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	public void addUser(String userName, def details) 
	{
		String resName = props.getPrivateUrl() + "user/" + userName;
		Resource person = model.createResource(resName);
		checkResource(person, RDF.type, Foaf.Person);
		checkResource(person, Triki.restricted, details."group");
		checkString(person, DCTerms.title, details."title");
		checkString(person, Foaf.mbox, details."email");
		checkString(person, Triki.login, details."login");
		checkString(person, Triki.password, details."password");
		checkResource(person, Foaf.member, details."member");
	}

}
