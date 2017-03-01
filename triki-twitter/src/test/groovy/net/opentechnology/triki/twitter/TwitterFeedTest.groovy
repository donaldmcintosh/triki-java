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

package net.opentechnology.triki.twitter


import groovy.util.logging.Log4j

import net.opentechnology.triki.news.async.NewsFeed
import net.opentechnology.triki.twitter.async.TwitterParser;

import org.apache.camel.ConsumerTemplate
import org.apache.camel.Produce
import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.Consume
import org.apache.camel.ProducerTemplate
import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Test;
import org.junit.Before

import java.text.SimpleDateFormat

@Log4j("logger")
class TwitterFeedTest extends CamelTestSupport {

	@Produce
	private ProducerTemplate pt;	
	private MockEndpoint resultEndpoint;
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		TwitterParser twitterBuilder = new TwitterParser();
		twitterBuilder.consumerKey=""
		twitterBuilder.consumerSecret=""
		twitterBuilder.accessToken=""
		twitterBuilder.accessTokenSecret=""
		
		return twitterBuilder;
	}
	
	
	public void testTwitterSuccess() {
		def newsItem = new NewsFeed(name: "indieweb", type: "twitter", dateFormat:"EEE MMM dd HH:mm:ss Z yyyy",
				keywords:"#indieweb")

		pt.sendBody("direct:twitter", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
	}
}
