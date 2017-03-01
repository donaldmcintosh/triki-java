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

import javax.inject.Inject;
import javax.inject.Named
import net.opentechnology.triki.sparql.SparqlExecutor
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.apache.commons.configuration.Configuration;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.springframework.beans.factory.annotation.Qualifier

abstract class NewsUpdateScheduler extends RouteBuilder
{
	String privateUrl;
	def refreshSchedule
	def refreshMilli
	def routeName
	
	@Override
	public void configure() throws Exception {
		SparqlExecutor sparqler = new SparqlExecutor();
		
		String queryString =
		"""
			PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
			PREFIX dc:    <http://purl.org/dc/terms/> 
            PREFIX type:  <${privateUrl}type/>
			SELECT ?sub ?url ?type ?title ?df ?link ?parser ?keywords
			 WHERE {  
				?sub a type:Feed . 
				?sub triki:feedurl ?url .
				?sub triki:feedtype ?type .
				?sub triki:refresh \"${refreshSchedule}\" .
				?sub dc:title ?title .
				OPTIONAL { ?sub triki:dateformat ?df . }
				OPTIONAL { ?sub triki:linkref ?link . }
				OPTIONAL { ?sub triki:itemParser ?parser . }
				OPTIONAL { ?sub triki:keywords ?keywords . }
			 } 
		""";
				
		
		RouteDefinition route = from("quartz2://${routeName}?trigger.repeatInterval=${refreshMilli}&trigger.repeatCount=-1")
		route.setId(routeName)
		route.split { Exchange exchange ->
				def feeds = []
				sparqler.execute(model, queryString) { QuerySolution soln ->
					RDFNode sub = soln.get("sub");
					RDFNode url = soln.get("url");
					RDFNode type = soln.get("type");
					RDFNode name = soln.get("title")
					RDFNode df = soln.get("df")
					RDFNode linkref = soln.get("link")
					RDFNode itemParser = soln.get("parser")
					RDFNode keywords = soln.get("keywords")
					
					def feedurl;
					if(url.isLiteral()){
						feedurl = url.asLiteral();
					}
					else
					{
						feedurl = url.asResource().getURI().toString();
					}
					
					def feed = new NewsFeed(name: name.asLiteral(), url:feedurl,
						type:type.asLiteral(), dateFormat: df.asLiteral(), linkClosure: linkref,
						itemParser: itemParser)
					if(keywords != null)
					{
						feed.keywords = keywords.asLiteral()
					}
					feeds += feed
				}
				exchange.out.body = feeds
			}
			.choice().when{Exchange e -> e.in.body.type == "rss"}
						.to("direct:rss")
					 .when{Exchange e -> e.in.body.type == "atom"}
						.to("direct:atom")
					.when{Exchange e -> e.in.body.type == "twitter"}
						.to("direct:twitter")
					.when{Exchange e -> e.in.body.type == "as2"}
						.to("direct:as2")
	}

}
