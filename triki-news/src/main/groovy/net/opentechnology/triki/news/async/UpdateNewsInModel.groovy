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

package net.opentechnology.triki.news.async

import org.apache.camel.Exchange

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.update.UpdateAction
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RSS

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.dto.GroupDto
import net.opentechnology.triki.core.model.ModelException;
import net.opentechnology.triki.core.model.ModelStore
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor
import groovy.util.logging.Log4j

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat
import java.util.Calendar;
import javax.inject.Inject

import org.apache.camel.builder.RouteBuilder
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value;

@Log4j("logger")
class UpdateNewsInModel extends RouteBuilder {
	
	@Inject @Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props

	@Inject
	private ModelStore store
	
	@Inject
	private GroupDto groupDto
	
	@Override
	public void configure() throws Exception {		
		from("direct:tomodel")
			.process { Exchange exchange ->
				SparqlExecutor deleter  = new SparqlExecutor();
				def items = exchange.in.body
				if(items.size() == 0){
					return
				}
				def source = items.first().source
				logger.info "Recieved ${items.size()} ${source} items"
				String newsBySource =
				"""
					PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
					PREFIX dc:    <http://purl.org/dc/terms/> 
					PREFIX rss:   <http://purl.org/rss/1.0/>
					DELETE { ?sub ?pred ?obj }
					WHERE {  
						?sub ?pred ?obj .
						?sub a rss:item . 
						?sub triki:source "$source" .
					 } 
				""";
				synchronized(model)
				{
					UpdateAction.parseExecute(newsBySource, model);
					
					items.each { Map item ->
						Resource newsItem = model.createResource(props.getPrivateUrl() + UUID.randomUUID(), RSS.item);
						newsItem.addLiteral(Triki.source, item.source)
						newsItem.addLiteral(RSS.link, item.url)
						newsItem.addLiteral(RSS.title, item.title)
						Calendar timestampCal = Calendar.getInstance();
						timestampCal.setTime(item.date);
						def timestampLiteral = model.createTypedLiteral(timestampCal);
						newsItem.addLiteral(DCTerms.created, timestampLiteral)
						newsItem.addProperty(Triki.restricted, groupDto.getGroup("public"))
						logger.debug("Added news item " + item.title)
					}
					
				}
			}
	}    

}
