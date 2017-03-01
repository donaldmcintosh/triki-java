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

package net.opentechnology.triki.share.async

import java.text.SimpleDateFormat

import javax.inject.Inject;

import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import groovy.util.logging.Log4j
import net.opentechnology.triki.core.dto.SettingDto;
import twitter4j.StatusJSONImpl

import net.opentechnology.triki.share.module.ShareModule

import org.springframework.aop.aspectj.RuntimeTestWalker.ThisInstanceOfResidueTestVisitor;
import org.springframework.beans.factory.annotation.Value;

@Log4j("logger")
class SendTwitter extends RouteBuilder
{
	@Inject
	private SettingDto settingDto;
	
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	
	@Override
	public void configure() throws Exception {
		
		callInterceptor()
		
		consumerKey = settingDto.getSetting(ShareModule.Settings.TWITTERCONSUMERKEY.name())
		consumerSecret = settingDto.getSetting(ShareModule.Settings.TWITTERCONSUMERSECRET.name())
		accessToken = settingDto.getSetting(ShareModule.Settings.TWITTERACCESSTOKEN.name())
		accessTokenSecret = settingDto.getSetting(ShareModule.Settings.TWITTERTOKENSECRET.name())
		
		from("seda:totwitter")
			.to("twitter://timeline/user?consumerKey=${consumerKey}&consumerSecret=${consumerSecret}&accessToken=${accessToken}&accessTokenSecret=${accessTokenSecret}")
			.process { Exchange exchange ->
				log.info("Twitter returned " + exchange.in.body)
			};
	}

	private callInterceptor() {
		if(this.metaClass.respondsTo(this, "interceptor"))
		{
			interceptor()
		}
	}



}
