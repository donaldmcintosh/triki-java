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

package net.opentechnology.triki.twitter.module;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Qualifier

import groovy.util.logging.Log4j;
import net.opentechnology.triki.schema.Triki

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.boot.StartupException;
import net.opentechnology.triki.core.dto.PageDto;
import net.opentechnology.triki.core.dto.PrefixDto;
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.core.dto.TypeDto;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.share.async.SendEmail;
import net.opentechnology.triki.share.async.SendTwitter
import net.opentechnology.triki.twitter.async.ChatterUpdateScheduler
import net.opentechnology.triki.twitter.async.TwitterParser

@Log4j
public class TwitterModule implements Module {
	
	public enum Settings {
		CHATREFRESH
	}
	
	@Inject	@Qualifier("siteModel")
	private Model model;	
	
	@Inject
	private CoreModule coreModule;

	@Inject
	private SettingDto settingDto;
	
	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private ChatterUpdateScheduler chatUpdateScheduler;
	
	@Inject
	private TwitterParser twitterParser;
	
	public void init()
	{
		coreModule.registerModules(this);
	}

	@Override
	public void initMod() {
		settingDto.addSetting(Settings.CHATREFRESH.name(), "10");
		
		initAsync();
	}
	
	public void initAsync() throws StartupException {
		try {
			camelCtx.addRoutes(twitterParser);
			camelCtx.addRoutes(chatUpdateScheduler);
		} catch (Exception e) {
			throw new StartupException(e);
		}
	}
	

	@Override
	public void initWeb() {
		// Nothing to do
	}

}
