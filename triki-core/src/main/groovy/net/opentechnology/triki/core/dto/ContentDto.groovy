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

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.ContentUtils;
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel;
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor
import net.opentechnology.triki.core.dto.SettingDto.Settings

public class ContentDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject @Qualifier("contentUtils")
	private ContentUtils contentUtils;
	
	public void addContent(String filename) 
	{
		String resName = props.getPrivateUrl() + "content/" + filename;
		Resource contentResource = model.createResource(resName);
		checkResource(contentResource, RDF.type, Triki.Content);
		checkString(contentResource, DCTerms.title, filename);
		checkResource(contentResource, Triki.restricted, settingDto.getSettingAsResource(Settings.RESTRICTION.name()))
	}
	
	public void checkFile(String filename, String defaultFilename)
	{
		if(!contentUtils.contentExists(filename))
		{
			addContent(filename)
			InputStream input = contentUtils.getInputStream(defaultFilename)
			contentUtils.writeStream(filename, input)
		}
	}

}
