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

package net.opentechnology.triki.core.boot;

import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.mockito.Matchers;
import org.mockito.Mock;

import net.opentechnology.triki.auth.AuthorisationManager;
import net.opentechnology.triki.core.model.ModelStore;

public class TrikiBaseTest {

	@Mock protected AuthorisationManager authMgr;
	protected Model model;
	protected Model vocabModel;
	@Mock protected ModelStore modelStore;
	@Mock protected Configuration config;
	@Mock protected HttpSession session;

	public void loadModel() throws FileNotFoundException {
		model = ModelFactory.createDefaultModel();
		InputStream in = FileUtils.openResourceFileAsStream("test.ttl");
		model.read(in, null, "TTL");
		
		vocabModel = ModelFactory.createDefaultModel();
		InputStream in2 = FileUtils.openResourceFileAsStream("test.ttl");
		vocabModel.read(in2, null, "TTL");
		authMgr.setModel(model);
		authMgr.setSession(session);
	}
	
	public void printModel(Model model) {
		model.write(System.out, "TURTLE");
	}

}
