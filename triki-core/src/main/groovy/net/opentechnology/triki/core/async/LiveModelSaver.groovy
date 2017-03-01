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

package net.opentechnology.triki.core.async

import java.text.SimpleDateFormat

import javax.inject.Inject;
import javax.inject.Named

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.core.model.ModelStore
import net.opentechnology.triki.schema.Exif;
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.apache.commons.configuration.Configuration;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value

import groovy.util.logging.Log4j;;

@Log4j("logger")
public class LiveModelSaver extends RouteBuilder
{
	@Inject @Qualifier("siteModel")
	private Model model;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private ModelStore store;
	
	@Inject
	private CachedPropertyStore props;
	
	@Override
	public void configure() throws Exception {
		String privateUrl = props.getPrivateUrl()
		int liveSavePeriod = settingDto.getSettingAsInteger(SettingDto.Settings.LIVEGRAPHSAVEPERIODMINS.name());
		int liveSaveMillis = liveSavePeriod * 60 * 1000
		RouteDefinition route = from("quartz2://livesave?trigger.repeatInterval=${liveSaveMillis}&trigger.repeatCount=-1")
		route.process { Exchange exchange ->
			store.saveModel(model, ModelStore.SITE_TTL);
		}
		logger.info("Configured async model saver every ${liveSavePeriod} minutes")
	}

}
