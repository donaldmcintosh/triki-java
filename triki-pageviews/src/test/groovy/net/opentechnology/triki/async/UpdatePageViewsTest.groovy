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

package net.opentechnology.triki.async

import static org.junit.Assert.*;

import org.junit.Test;

class UpdatePageViewsTest {

	private String ignores = "(?iu).*(bot|spider|curl|feedly|crawl|82.12.121.20|NetShelter ContentScan|slurp).*"
	
	@Test
	public void testSpider() {
		
		assertTrue "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)".matches(ignores);
	}
	
	@Test
	public void testCrawler() {
		
		assertTrue "Mozilla/5.0 (compatible; MegaIndex.ru/2.0; +http://megaindex.com/crawler)".matches(ignores);
	}

	@Test
	public void testBot() {
		
		assertTrue "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)".matches(ignores);
	}
	
	@Test
	public void testBotCase() {
		
		assertTrue "Mozilla/5.0 (compatible; GoogleBOT/2.1; +http://www.google.com/BOT.html)".matches(ignores);
	}
	
	@Test
	public void testFirefox() {
		
		assertFalse "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0".matches(ignores);
	}
	
}
