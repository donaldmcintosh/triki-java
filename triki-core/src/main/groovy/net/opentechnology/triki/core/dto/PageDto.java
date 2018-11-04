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

import java.util.Calendar;

import javax.inject.Inject;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.schema.Triki;

public class PageDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private GroupDto groupDto;

	public Resource addPage(String name, Resource type, String title, String restricted) {
		String resName = props.getPrivateUrl() + name;
		Resource page = model.createResource(resName);
		checkResource(page, RDF.type, type);
		checkString(page, DCTerms.title, title);
		checkResource(page, Triki.restricted, groupDto.getGroup(restricted));

		return page;
	}
	
	public void addIndexPage(String name, Resource type, String title, String restricted, Resource indexType)
	{
		Resource page = addPage(name, type, title, restricted);
		checkResource(page, DCTerms.references, indexType);
	}

}
