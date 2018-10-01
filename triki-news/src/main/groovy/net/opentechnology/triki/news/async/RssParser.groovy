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


import java.text.ParseException
import java.text.SimpleDateFormat
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import groovy.util.logging.Log4j

@Log4j("logger")
class RssParser extends RouteBuilder
{
	@Override
	public void configure() throws Exception {
		from("direct:rss")
			.process { Exchange exchange ->
				def items = []
				NewsFeed src = exchange.in.body
				def rssRoot = new XmlParser(false, false).parseText(src.url.toURL().getText(requestProperties: ['User-Agent': 'triki'], "UTF-8"))
				if(!src.itemParser)
				{
					rssRoot.channel.each { channel ->
						channel.item.each { item ->
							def news = [:]
							news.source = src.name
							news.title = item.title.text()
							try {
								news.date = new SimpleDateFormat(src.dateFormat).parse(item.pubDate.text().trim())
							}
							catch(ParseException pe)
							{
								logger.error "Error parsing RSS from " + src.name
								logger.error pe.getMessage()
							}
							if(!src.linkClosure)
							{
								news.url = item.link.text()
							}
							else
							{
								news.url = new LinkReader()."${src.linkClosure}"(item)
							}
							items += news
						}
					}
				}
				else
				{
					log.info("Running override itemParser ${src.itemParser}")
					items = new LinkReader()."${src.itemParser}"(src, items, rssRoot)
				}
				exchange.out.body = items
			}
		.to("direct:tomodel")
	}
}
