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

package net.opentechnology.triki.news


import groovy.util.logging.Log4j
import net.opentechnology.triki.news.async.AtomParser;
import net.opentechnology.triki.news.async.LinkReader;
import net.opentechnology.triki.news.async.NewsFeed;
import net.opentechnology.triki.news.async.RssParser;

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
class NewsFeedTest extends CamelTestSupport {

	@Produce
	private ProducerTemplate pt;	
	private MockEndpoint resultEndpoint;
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		RouteBuilder builder = new RssParser();
		RouteBuilder atomBuilder = new AtomParser();
		
		RouteBuilder testRouter = new RouteBuilder(){
			public void configure() throws Exception
			{
				from("direct:tomodel").to("mock:rssitems")
			};
		};
		builder.includeRoutes(testRouter);
		builder.includeRoutes(atomBuilder);
		return builder;
	}
	
	@Before
	public void setup()
	{
		LinkReader.metaClass.clausLink = { entry ->
			entry.link.findAll { link ->
				link."@rel" == "alternate"
			}.first()."@href"
		}
		LinkReader.metaClass.edgeParser = { src, items, rssRoot ->
			rssRoot."**".findAll { node ->
					node.name() == 'item'
				}.each { item ->
					def news = [:]
					news.source = src.name
					news.title = item.title.text()
					news.url = item.link.text()
					news.date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").parse(item."pubDate".text().trim())
					items += news
				}
			return items
		}
		LinkReader.metaClass.lxerParser = { src, items, rssRoot ->
			rssRoot."**".findAll { node ->
					node.name() == 'item'
				}.each { item ->
					def news = [:]
					news.source = src.name
					news.title = item.title.text()
					news.url = item.link.text()
					news.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(item."dc:date".text().trim())
					items += news
				}
			return items
		}
		resultEndpoint = context.getEndpoint("mock:rssitems", MockEndpoint.class);
	}

	@Test
	public void testRssNewsFeedSuccess() {
		def newsItem = new NewsFeed(name: "weather", type: "rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z")
		newsItem.url = getClass().getClassLoader().getResource("newssource/weather.rss").toString()
		pt.sendBody("direct:rss", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
		assert exchanges.size() == 1
		def items = exchanges.first().in.body
		assert items.size() == 3
		items.each { item ->
			assert item.source == "weather"
		}
		assert items[0].title == "Monday: Sunny Intervals, Maximum Temperature: 8\u00B0C (46\u00B0F) Minimum Temperature: -2\u00B0C (28\u00B0F)"
	}
	
	public void testGuardianNewsFeedSuccess() {
		def newsItem = new NewsFeed(name: "guardian", type: "rss", url:"https://www.theguardian.com/uk/rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z")
		//def newsItem = new NewsFeed(name: "guardian", type: "rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z")
		//newsItem.url = getClass().getClassLoader().getResource("newssource/guardian.rss").toString()
		pt.sendBody("direct:rss", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
	}
	

	@Test
	public void testAtomNewsFeedSuccess() {
		// Mon, 07 Mar 2016 09:14:55 -0600
		def newsItem = new NewsFeed(name: "scottadams", type: "rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z")
		newsItem.url = getClass().getClassLoader().getResource("newssource/scottadams.xml").toString()
		pt.sendBody("direct:rss", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
		assert exchanges.size() == 1
		def items = exchanges.first().in.body
		assert items.size() == 20
		items.each { item ->
			assert item.source == "scottadams"
		}
		assert items[0].title == "The Conservative Con (Master Persuader Series)"
	}
	
	@Test
	public void testClausAtomNewsFeedSuccess() {
		def newsItem = new NewsFeed(name: "claus", type: "atom", 
			dateFormat:"yyyy-MM-dd'T'HH:mm:ss.SSSXXX", linkClosure: "clausLink")
		newsItem.url = getClass().getClassLoader().getResource("newssource/claus.xml").toString()
		pt.sendBody("direct:atom", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
		assert exchanges.size() == 1
		def items = exchanges.first().in.body
		assert items.size() == 25
		items.each { item ->
			assert item.source == "claus"
		}
		assert items[0].title == "Help tell Pivotal to accept Apache Camel as a choice on start.spring.io"
		assert items[0].url == "http://feedproxy.google.com/~r/ApacheCamel/~3/NwzwI2c4bTo/help-tell-pivotal-to-accept-apache_30.html"
	}
	
	@Test
	public void testLxerRSSFeedSuccess() {
		def newsItem = new NewsFeed(name: "lxer", type: "rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z",
								itemParser: "lxerParser")
		newsItem.url = getClass().getClassLoader().getResource("newssource/lxer.rss").toString()
		pt.sendBody("direct:rss", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
		assert exchanges.size() == 1
		def items = exchanges.first().in.body
		assert items.size() == 20
		items.each { item ->
			assert item.source == "lxer"
		}
		assert items[0].title == "Tiny ARM9 COM and SBC support dual Ethernet and CAN"
	}
	
	@Test
	public void testEdgeRSSFeedSuccess() {
		def newsItem = new NewsFeed(name: "edge", type: "rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z",
								itemParser: "edgeParser")
		newsItem.url = getClass().getClassLoader().getResource("newssource/edge.xml").toString()
		pt.sendBody("direct:rss", newsItem)
		
		def exchanges = resultEndpoint.getExchanges();
		assert exchanges.size() == 1
		def items = exchanges.first().in.body
		assert items.size() == 10
		items.each { item ->
			assert item.source == "edge"
		}
		assert items[0].title == "Is Big Data Taking Us Closer to the Deeper Questions in Artificial Intelligence?"
	}
	

}
