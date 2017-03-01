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

import javax.inject.Inject;

import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.news.async.NewsFeed
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import groovy.util.logging.Log4j
import twitter4j.StatusJSONImpl

import net.opentechnology.triki.share.module.ShareModule

import org.springframework.beans.factory.annotation.Value;

@Log4j("logger")
class TwitterParser extends RouteBuilder
{
	@Inject
	private SettingDto settingDto;
	
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	
	@Override
	public void configure() throws Exception {
		consumerKey = settingDto.getSetting(ShareModule.Settings.TWITTERCONSUMERKEY.name())
		consumerSecret = settingDto.getSetting(ShareModule.Settings.TWITTERCONSUMERSECRET.name())
		accessToken = settingDto.getSetting(ShareModule.Settings.TWITTERACCESSTOKEN.name())
		accessTokenSecret = settingDto.getSetting(ShareModule.Settings.TWITTERTOKENSECRET.name())
		
		from("direct:twitter")
		.process { Exchange exchange ->
			NewsFeed src = exchange.in.body
			exchange.out.headers.put("src", src)
			exchange.out.headers.put("CamelTwitterKeywords", src.keywords)
		}
		.to("twitter://search?consumerKey=${consumerKey}&consumerSecret=${consumerSecret}&accessToken=${accessToken}&accessTokenSecret=${accessTokenSecret}")
		.process { Exchange exchange ->
			NewsFeed src = exchange.in.headers.get("src")
			def items = []
			List<StatusJSONImpl> msgs = exchange.in.body
			msgs.each { StatusJSONImpl msg ->
				def news = [:]
				def user = msg.user
				news.source = src.name
				news.title = user.name + ": " + msg.text + " RT: " + msg.retweetCount
				news.date = msg.createdAt
				if(msg.urlEntities.length > 0)
				{
					news.url = msg.urlEntities[0].expandedURL
				}
				else
				{
					news.url = "http://www.twitter.com"
				}
				items += news
			}
			
			exchange.out.body = items
		}
		.to("direct:tomodel")
	}

}
