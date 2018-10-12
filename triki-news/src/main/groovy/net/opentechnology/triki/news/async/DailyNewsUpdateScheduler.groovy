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

import net.opentechnology.triki.core.boot.CachedPropertyStore

import java.text.SimpleDateFormat
import javax.inject.Inject

import org.apache.camel.Exchange
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value

import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.news.module.NewsModule

class DailyNewsUpdateScheduler extends NewsUpdateScheduler
{
	@Inject @Qualifier("siteModel")
	private Model model;

	@Inject
	private CachedPropertyStore props
	
	@Inject
	private SettingDto settingDto;
	
	private int dailyRefresh;
	
	@Override
	public void configure() throws Exception {
		privateUrl = props.getPrivateUrl()
		dailyRefresh = settingDto.getSettingAsInteger(NewsModule.Settings.DAILYREFRESH.name())
		routeName = "dailyNewsUpdate"
		refreshSchedule = "daily"
		refreshMilli = dailyRefresh * 60 * 1000
		
		super.configure()
	}

}
