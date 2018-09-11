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

package net.opentechnology.triki.auth;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.opentechnology.triki.core.boot.TrikiBaseTest;
import net.opentechnology.triki.core.dto.GroupDto;
import net.opentechnology.triki.core.model.DateIndexer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.opentechnology.triki.core.boot.Utilities;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationManagerTest extends TrikiBaseTest {

	AuthorisationManager authoriser;
	
//	@Before
//	public void loadModel() throws FileNotFoundException{
//		model = ModelFactory.createDefaultModel();
//		InputStream in = FileUtils.openResourceFileAsStream("test.ttl");
//		model.read(in, null, "TTL");
//	}

	@Before
	public void setup() throws FileNotFoundException {
		loadModel();
		authoriser = new AuthorisationManager();
		authoriser.setModel(model);
		GroupDto groupDto = new GroupDto();
		groupDto.setModel(model);
		groupDto.setProps(cachedPropertyStore);
		authoriser.setGroupDto(groupDto);
		when(config.getString("private_url")).thenReturn("http://www.donaldmcintosh.net");
		when(cachedPropertyStore.getPrivateUrl()).thenReturn("http://www.donaldmcintosh.net/");
	}

	@Test
	public void testAuthorisedOneGroup() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/ibiza2014";
		Resource person = model.getResource("http://www.donaldmcintosh.net/resource/joe+bloggs");
		Assert.assertTrue(authoriser.authorise(targetUrl, person));
	}
//
//	private AuthorisationManager initAuthoriser() {
//		AuthorisationManager authoriser = new AuthorisationManager();
//		authoriser.setModel(model);
//		GroupDto groupDto = new GroupDto();
//		groupDto.setModel(model);
//		authoriser.setGroupDto(groupDto);
//		return authoriser;
//	}

	@Test
	public void testAuthorisedAnotherGroup() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/ibiza2014";
		Resource person = model.getResource("http://www.donaldmcintosh.net/resource/donald+mcintosh");
		Assert.assertTrue(authoriser.authorise(targetUrl, person));
	}
	
	@Test
	public void testNotAuthorised() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/aug2014";
		Resource person = model.getResource("http://www.donaldmcintosh.net/resource/joe+bloggs");
		Assert.assertFalse(authoriser.authorise(targetUrl, person));
	}
	
	@Test
	public void testBadResource() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/blah";
		Resource person = model.getResource("http://www.donaldmcintosh.net/resource/joe+bloggs");
		Assert.assertFalse(authoriser.authorise(targetUrl, person));
	}
	
	@Test
	public void testUnrestrictedJpgResource() {
		String targetUrl = "http://www.donaldmcintosh.net/content/123.jpg";
		Assert.assertTrue(authoriser.unrestricted(targetUrl));
	}
	
	@Test
	public void testUnrestrictedCssResource() {
		String targetUrl = "http://www.donaldmcintosh.net/content/123.css";
		Assert.assertTrue(authoriser.unrestricted(targetUrl));
	}
	
	@Test
	public void testUnrestrictedSvgResource() {
		String targetUrl = "http://www.donaldmcintosh.net/content/123.svg";
		Assert.assertTrue(authoriser.unrestricted(targetUrl));
	}

	@Test
	public void testUnrestrictedJsResource() {
		String targetUrl = "http://www.donaldmcintosh.net/content/123.js";
		Assert.assertTrue(authoriser.unrestricted(targetUrl));
	}
	
	@Test
	public void testUnrestrictedHtmlResource() {
		String targetUrl = "http://www.donaldmcintosh.net/content/123.html";
		Assert.assertTrue(authoriser.unrestricted(targetUrl));
	}
	
	@Test
	public void testRestrictedHtmlResource() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/123.html";
		Assert.assertFalse(authoriser.unrestricted(targetUrl));
	}
	
	@Test
	public void testPublicResource() {
		String targetUrl = "http://www.donaldmcintosh.net/resource/ibiza2013";
		Assert.assertTrue(authoriser.publicAccess(targetUrl));
	}
}
