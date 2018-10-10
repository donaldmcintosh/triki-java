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


import java.text.SimpleDateFormat
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import groovy.util.logging.Log4j

@Log4j("logger")
class AtomParser extends RouteBuilder
{
	@Override
	public void configure() throws Exception {
		from("direct:atom")
		.process { Exchange exchange ->
			def items = []
			NewsFeed src = exchange.in.body
			def feedRoot = new XmlParser(false, false).parseText(src.url.toURL().getText(requestProperties: ['User-Agent': 'triki'], "UTF-8"))
			feedRoot.entry.each { entry ->
				entry.title.each { title ->
					def news = [:]
					news.source = src.name
					news.title = entry.title.text()
					news.date = new SimpleDateFormat(src.dateFormat).parse(entry.updated.text())
					if(!src.linkClosure)
					{
						news.url = entry.link."@href"
					}
					else
					{
						news.url = new LinkReader()."${src.linkClosure}"(entry)
					}
					items += news
				}
			}
			exchange.out.body = items
		}
		.to("direct:tomodel")
	}

}
