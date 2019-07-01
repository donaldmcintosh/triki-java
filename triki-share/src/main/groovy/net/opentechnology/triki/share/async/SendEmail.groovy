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

package net.opentechnology.triki.mtd.async


import javax.inject.Inject

import org.apache.camel.builder.RouteBuilder
import groovy.util.logging.Log4j
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.share.module.ShareModule.Settings

@Log4j("logger")
class SendEmail extends RouteBuilder
{
	@Inject
	private SettingDto settingDto;
	
	private String emailUsername;	
	private String emailPassword;
	private String imapHost;	
	private String emailFrom;
	
	@Override
	public void configure() throws Exception {
		callInterceptor()
		
		emailUsername = settingDto.getSetting(Settings.EMAILUSERNAME.name())
		emailPassword = settingDto.getSetting(Settings.EMAILPASSWORD.name())
		imapHost = settingDto.getSetting(Settings.IMAPHOST.name())
		emailFrom = settingDto.getSetting(Settings.EMAILFROM.name())
		
		from("seda:toemail")
			.to("smtps://${emailUsername}@${imapHost}?password=${emailPassword}");
	}

	private callInterceptor() {
		if(this.metaClass.respondsTo(this, "interceptor"))
		{
			interceptor()
		}
	}



}
