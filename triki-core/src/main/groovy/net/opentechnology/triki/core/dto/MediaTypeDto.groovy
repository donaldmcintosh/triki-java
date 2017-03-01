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

import java.io.File;

import javax.inject.Inject
import javax.ws.rs.core.MediaType;

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel;
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class MediaTypeDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public void addMediaType(String httpHeader, String suffix, String maxage) 
	{
		String resName = props.getPrivateUrl() + "mediatype/" + suffix;
		Resource mediaType = model.createResource(resName);
		checkResource(mediaType, RDF.type, Triki.MediaType);
		checkString(mediaType, DCTerms.title, suffix);
		checkString(mediaType, DCTerms.format, httpHeader);
		checkString(mediaType, Triki.maxage, maxage);
		createDir(suffix);
	}
	
	public void createDir(String suffix)
	{
		File dirFile = new File(props.getContentDir() + File.separator + suffix);
		if(!dirFile.exists())
		{
			logger.info("Creating directory " + dirFile.getAbsolutePath());
			dirFile.mkdirs();
		}
	}
	
	public boolean isTextMediaType(String url)
	{
		String mediaType = getMediaTypeForUrl(url)
		return mediaType.contains("text");
	}
	
	public String getMediaTypeForUrl(String url)
	{
		String suffix = url.replaceAll("^.*\\.", "")
		return getMediaTypeForSuffix(suffix);
	}
	
	public String getMediaTypeForSuffix(String suffix)
	{
		String med = MediaType.TEXT_PLAIN
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?mediaType
		WHERE {  
			 ?sub a triki:MediaType .
			 ?sub dc:format ?mediaType .
			 ?sub dc:title "${suffix}" .
        }
"""
		
		sparqler.execute(model, query){ QuerySolution soln ->
			med = soln.get("mediaType").asLiteral().toString()
		}
			
		return med;
	}
	
	public String getMaxageForSuffix(String suffix)
	{
		String maxAge = "1"
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?maxAge
		WHERE {  
			 ?sub a triki:MediaType .
			 ?sub triki:maxage ?maxAge .
			 ?sub dc:title "${suffix}" .
        }
"""
		
		sparqler.execute(model, query){ QuerySolution soln ->
			maxAge = soln.get("maxAge").asLiteral().toString()
		}
			
		return maxAge;
	}
	
	private mediaTypeHandling(NodeFormModel formModel) {
		if(formModel.linkProperties.any {
			LinkProperty link -> link.getPropertyField().property == RDF.type &&
			link.getValueField().link == Triki.MediaType })
		{
			TextProperty format = formModel.textProperties.find { TextProperty text -> text.getPropertyField().property == DCTerms.format }
			if (format == null)
			{
				formModel.errors << "A Media Type must define a format."
			}
			else
			{
				String suffix = format.valueField.text
				createDir(suffix)
				formModel.msgs << "Successfully created a new directory for ${suffix} content."
			}
		}
	}

}
