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

import java.text.SimpleDateFormat
import java.util.Calendar;

import javax.inject.Inject
import javax.inject.Named
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.commons.configuration.Configuration;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.update.UpdateAction
import org.apache.jena.vocabulary.DCTerms
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;

import groovy.util.logging.Log4j
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.schema.Activitystreams2
import net.opentechnology.triki.schema.Exif;
import net.opentechnology.triki.sparql.SparqlExecutor
import net.opentechnology.triki.schema.Rdfbase
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;

@Log4j("logger")
public class AS2Creator extends RouteBuilder
{	
	@Inject @Qualifier("siteModel")
    private Model siteModel;
	
	@Inject @Qualifier("asModel")
    private Model asModel;
	private final Utilities utils;
	
	@Value('${private_url}')
	private String privateUrl;
	
	@Value('${public_url}')
	private String publicUrl;
	
	@Inject
	public AS2Creator(Utilities utils){
		this.utils = utils;
	}
	
	@Override
	public void configure() throws Exception {
		int i = 0
		
		asModel.setNsPrefix("resource", publicUrl + "/resource/");
		asModel.setNsPrefix("as", Activitystreams2.NS);
		asModel.setNsPrefix("exif", Exif.NS);
		asModel.setNsPrefix("dc", DCTerms.NS);
		asModel.setNsPrefix("triki", Triki.NS);
		asModel.setNsPrefix("time", Time.NS);
		asModel.setNsPrefix("foaf", FOAF.NS);
		asModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		
		from("direct:as2")
		.process { Exchange exchange ->
			def items = []
			NewsFeed src = exchange.in.body
			
			String deleteAll =
			"""
					PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
					PREFIX dc:    <http://purl.org/dc/terms/> 
					PREFIX rss:   <http://purl.org/rss/1.0/>
					DELETE { ?sub ?pred ?obj }
					WHERE {  
						?sub ?pred ?obj .
					 } 
				""";
				
			synchronized(asModel)
			{
				UpdateAction.parseExecute(deleteAll, asModel);

				String collUrl = makeUrlPublic(src.url, asModel);
				Resource collItem = asModel.createResource(collUrl);
				collItem.addProperty(Rdfbase.type, Activitystreams2.Collection);
				
				SimpleDateFormat formatter = new SimpleDateFormat(src.dateFormat);
				Calendar timestampCal = Calendar.getInstance();
				timestampCal.setTimeInMillis(new Date().time);
				timestampCal.add(Calendar.DATE, -14);
				String aWeekAgo = formatter.format(timestampCal.getTime());
				String type = "resource:blog"
				
				String queryString = """
					PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
					PREFIX dc:    <http://purl.org/dc/terms/> 
					PREFIX rss:   <http://purl.org/rss/1.0/>
					PREFIX resource: <${privateUrl}/resource/>
					PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
					
					SELECT ?sub ?created ?creator ?title ?name ?parent
					
					WHERE { 
						?sub a ${type} . 
						?parent dc:references ${type} .
						?sub dc:created ?created .
						?sub dc:creator ?creator .
						?sub dc:title ?title .
						?creator dc:description ?name.
						?sub triki:restricted triki:public .
						FILTER (  ?created > "${aWeekAgo}"^^xsd:dateTime )
					} 
					ORDER BY asc(?created)
				""";
				
				SparqlExecutor sparqler = new SparqlExecutor();
				sparqler.execute(siteModel, queryString) { QuerySolution soln ->
					RDFNode sub = soln.get("sub");
					RDFNode created = soln.get("created");
					RDFNode name = soln.get("name");
					RDFNode creator = soln.get("creator");
					RDFNode parent = soln.get("parent");
					Resource subRes = sub.asResource();
					Resource item = asModel.createResource();
					item.addProperty(Rdfbase.type, Activitystreams2.Add);
					item.addProperty(Activitystreams2.published, created.asLiteral());
					item.addProperty(Activitystreams2.name, "${name} added a new ${type}.");
					item.addProperty(Activitystreams2.actor, makePublic(asModel, creator));
					item.addProperty(Activitystreams2.object, makePublic(asModel, sub));
					item.addProperty(Activitystreams2.target, makePublic(asModel, parent));
					
					collItem.addProperty(Activitystreams2.items, item);
					i++
				}
				
				collItem.addProperty(Activitystreams2.totalItems, i);
				logger.info("Added ${i} items to my Activity Stream")
			}
			
			utils.saveSite(asModel, "/tmp/as.ttl");
		}
	}
	
	public Resource makePublic(Model model, Resource privateResource)
	{
		String url = privateResource.URI;
		String newurl = makeUrlPublic(url, model);
		Resource resource = model.createResource(newurl);
		return resource;
	}

	private String makeUrlPublic(String url, Model model) {
		String newurl = url.replaceAll(privateUrl, publicUrl);
		return newurl;
	}

}
