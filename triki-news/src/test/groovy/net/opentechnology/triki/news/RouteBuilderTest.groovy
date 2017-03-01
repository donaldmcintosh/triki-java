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


import static org.junit.Assert.*;

import org.apache.camel.Produce
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.commons.configuration.Configuration;
import org.apache.http.client.utils.URIBuilder
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import net.opentechnology.triki.news.async.NewsFeed;
import net.opentechnology.triki.news.async.UpdateNewsInModel;

/*
 * TODO - Try this as Spock
 */
//@RunWith(MockitoJUnitRunner.class)
class RouteBuilderTest extends CamelTestSupport {
	
	@Produce
	private ProducerTemplate pt;
	
	@Mock
	Model siteModel
	
	@Mock
	Configuration config

	public UpdateNewsInModel createRouteBuilder()
	{
		return new UpdateNewsInModel(siteModel, config);	
	}
	
	public void testRss() {
		NewsFeed herald = [name:"herald", url:"http://www.heraldscotland.com/news/rss/", type:"rss", dateFormat:"EEE, dd MMM yyyy HH:mm:ss Z"]
		pt.sendBody("direct:rss", herald)
	}
	
	public void testAtom() {
		NewsFeed register = [name:"register", url:"http://www.theregister.co.uk/headlines.atom"]
		pt.sendBody("direct:atom", register)
	}

	public void testStock() {
		def uri = new URIBuilder("http://query.yahooapis.com/v1/public/yql")
		uri.addParameter("q", """
			select * from   yahoo.finance.historicaldata
	         where  symbol    = "SSE.L"
	         and    startDate = "2015-12-01"
	         and    endDate   = "2016-02-30"
			""")
		uri.addParameter("format", "xml")
		uri.addParameter("env", "store://datatables.org/alltableswithkeys")
		NewsFeed yahoo = [name:"yahoo", url:uri.build()]
		pt.sendBody("direct:yahoo", yahoo)
	}

}
