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
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationManagerTest {
	
	private Model model;
	
	@Before
	public void loadModel() throws FileNotFoundException{
		model = ModelFactory.createDefaultModel();
		InputStream in = FileUtils.openResourceFileAsStream("test.ttl");
		model.read(in, null, "TTL");
	}

	@Test
	public void testSuccess() throws AuthenticationException {
		String email = "joe.bloggs@yahoo.co.uk";
		String login = "donald";
		String password = "donald2";
		AuthenticationManager authenticator = new AuthenticationManager();
		authenticator.setModel(model);
		Optional<Resource> person = authenticator.authenticate(login, password);
		Resource expected = model.getResource("http://www.donaldmcintosh.net/resource/joe+bloggs");
		Assert.assertTrue(person.get().equals(expected));
	}

	@Test
	public void testFailureMissingEmail() throws AuthenticationException {
		String email = "fred.bloggs@yahoo.co.uk";
		String password = "password123";
		AuthenticationManager authenticator = new AuthenticationManager();
		authenticator.setModel(model);
		Optional<Resource> person = authenticator.authenticate(email, password);
		assert !person.isPresent();
	}

	@Test
	public void testFailureBadPassword() throws AuthenticationException {
		String email = "joe.bloggs@yahoo.co.uk";
		String password = "guess";
		AuthenticationManager authenticator = new AuthenticationManager();
		authenticator.setModel(model);
		Optional<Resource> person = authenticator.authenticate(email, password);
		assert !person.isPresent();
	}
}
