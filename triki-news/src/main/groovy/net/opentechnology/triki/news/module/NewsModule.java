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

package net.opentechnology.triki.news.module;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.beans.factory.annotation.Qualifier;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.boot.StartupException;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.news.async.AtomParser;
import net.opentechnology.triki.news.async.DailyNewsUpdateScheduler;
import net.opentechnology.triki.news.async.LiveNewsUpdateScheduler;
import net.opentechnology.triki.news.async.RssParser;
import net.opentechnology.triki.news.async.UpdateNewsInModel;
import net.opentechnology.triki.news.init.LoadTrikiExtensions;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.core.dto.PropertyDto;
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.core.dto.TypeDto;

public class NewsModule implements Module {
	
	public enum Settings {
		NEWSREFRESH,
		DAILYREFRESH
	}
	
	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private LiveNewsUpdateScheduler liveUpdater;
	
	@Inject
	private DailyNewsUpdateScheduler dailyUpdater;
	
	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private UpdateNewsInModel newsUpdater;
	
	@Inject
	private RssParser rssParser;
	
	@Inject
	private AtomParser atomParser;
	
	@Inject
	private LoadTrikiExtensions extensions;
	
	public void init()
	{
		coreModule.registerModules(this);
	}

	@Override
	public void initMod() throws StartupException {
		extensions.init();
		
		model.setNsPrefix("rss", "http://purl.org/rss/1.0/");
		model.setNsPrefix("as", "http://www.w3.org/ns/activitystreams#");
		
		settingDto.addSetting(Settings.NEWSREFRESH.name(), "15");
		settingDto.addSetting(Settings.DAILYREFRESH.name(), "720");
		
		propertyDto.addProperty("dateformat", Triki.dateformat.getURI(), 11);
		propertyDto.addProperty("feedtype", Triki.feedtype.getURI(), 11);
		propertyDto.addProperty("feedurl", Triki.feedurl.getURI(), 11);
		propertyDto.addProperty("itemParser", Triki.itemParser.getURI(), 11);
		propertyDto.addProperty("refresh", Triki.refresh.getURI(), 11);
		
		typeDto.addType("Feed", Triki.Feed.getURI());
		
		initAsync();
	}
	
	public void initAsync() throws StartupException {
		try {
			camelCtx.addRoutes(rssParser);
			camelCtx.addRoutes(atomParser);
			camelCtx.addRoutes(newsUpdater);
			camelCtx.addRoutes(liveUpdater);
			camelCtx.addRoutes(dailyUpdater);
		} catch (Exception e) {
			throw new StartupException(e);
		}
	}
	
	@Override
	public void initWeb() {
		// Nothing to do
	}

}
