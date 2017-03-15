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

package net.opentechnology.triki.pageviews.async

import org.apache.camel.Exchange

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.update.UpdateAction
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RSS

import net.opentechnology.triki.pageviews.module.PageViewsModule

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.dto.SettingDto;
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
class UpdatePageViews extends RouteBuilder {
	
	@Inject @Qualifier("siteModel")
	private Model model;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private CachedPropertyStore props;
	
	private String ignoreAgents;
	
	@Override
	public void configure() throws Exception {	
		ignoreAgents = settingDto.getSetting(PageViewsModule.Settings.IGNOREAGENTS.name())
		from("seda:pageviews")
			.process { Exchange exchange ->
				int views = 0;
				SparqlExecutor sparqler  = new SparqlExecutor();
				PageView view = exchange.in.body
				
				String ignores = ignoreAgents.replaceAll("'", "");
				
				if(view.forwardedFor != null && !view.getAgent().matches(ignores)){
					Resource page = model.getResource(view.url);
					if(model.containsResource(page))
					{
						if(page.hasProperty(Triki.pageViews))
						{
							Statement pageViews = page.getProperty(Triki.pageViews);
							int viewCount = pageViews.getObject().asLiteral().getInt();
							page.removeAll(Triki.pageViews);
							page.addProperty(Triki.pageViews, ++viewCount);
						}
						else 
						{
							page.addProperty(Triki.pageViews, "1");
						}
					}
				}

			}
	}    

}
