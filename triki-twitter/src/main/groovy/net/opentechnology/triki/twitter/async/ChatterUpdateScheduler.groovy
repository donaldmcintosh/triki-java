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

package net.opentechnology.triki.twitter.async

import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Named

import net.opentechnology.triki.news.init.LoadTrikiExtensions
import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.news.async.NewsUpdateScheduler
import net.opentechnology.triki.sparql.SparqlExecutor
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.commons.configuration.Configuration;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value;

import net.opentechnology.triki.twitter.module.TwitterModule

class ChatterUpdateScheduler extends NewsUpdateScheduler
{
	@Inject @Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props
	
	private int chatterRefresh;
	
	@Inject
	private SettingDto settingDto;
	
	@Override
	public void configure() throws Exception {
		privateUrl = props.getPrivateUrl()
		chatterRefresh = settingDto.getSettingAsInteger(TwitterModule.Settings.CHATREFRESH.name())
		
		routeName = "chatterUpdate"
		refreshSchedule = "chatter"
		refreshMilli = chatterRefresh * 60 * 1000
		
		super.configure()
	}

}
