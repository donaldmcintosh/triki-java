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

import javax.inject.Inject;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Qualifier;

public class AuthenticationManager {

	@Inject	@Qualifier("siteModel")
    private Model model;
	
	public Resource authenticate(String login, String password) throws AuthenticationException {
		Resource person = getPersonByLogin(login, password);
		return person;
	}
	
	public Resource authenticateByWebsite(String website) throws AuthenticationException {
		Resource person = getPersonByWebsite(website);
		return person;
	}

	public Resource authenticateByNameAndEmail(String name, String email) throws AuthenticationException {
		Resource person = getPersonByNameAndEmail(name, email);
		return person;
	}
	
	private boolean checkPassword(String password) {
		boolean auth = false;
		String queryString = 
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?url " +
				"WHERE {" +
				"      ?url triki:password \""+ password + "\" . " +
				"      }";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution soln = results.next();
			Resource url = soln.getResource("url");
			auth = true;
		}
		else {
			auth = false;
		}		
		
		qe.close();
		return auth;
	}

	private Resource getPersonByLogin(String login, String password) throws AuthenticationException{
		String queryString = 
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?person " +
				"WHERE {" +
				"  ?person a foaf:Person . " +
				"  ?person triki:login \""+ login + "\" . " +
				"  ?person triki:password \""+ password + "\" . " +
				"  }";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution soln = results.next();
			Resource person = soln.getResource("person");
			qe.close();	
			return person;
		}
		else {
			qe.close();	
			throw new AuthenticationException("Could not find person with login " + login);
		}
	}
	
	private Resource getPersonByWebsite(String website) throws AuthenticationException{
		String queryString =
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"PREFIX triki: <http://www.opentechnology.net/triki/0.1/> " +
				"SELECT ?person " +
				"WHERE {" +
				"  ?person a foaf:Person . " +
				"  ?person foaf:homepage \""+ website + "\" . " +
				"  }";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution soln = results.next();
			Resource person = soln.getResource("person");
			qe.close();
			return person;
		}
		else {
			qe.close();
			throw new AuthenticationException("Could not find person with id " + website);
		}
	}

	private Resource getPersonByNameAndEmail(String name, String email) throws AuthenticationException{
		String queryString =
						"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
						"PREFIX dc: <http://purl.org/dc/terms/> " +
						"SELECT ?person " +
						"WHERE {" +
						"  ?person a foaf:Person . " +
						"  ?person foaf:mbox \""+ email + "\" . " +
						"  ?person dc:title \""+ name + "\" . " +
						"}";

		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution soln = results.next();
			Resource person = soln.getResource("person");
			qe.close();
			return person;
		}
		else {
			qe.close();
			throw new AuthenticationException("Could not find person with name " + name + " and email " + email);
		}
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
