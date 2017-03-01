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

package net.opentechnology.triki.auth.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import groovy.util.logging.Log4j
import javax.inject.Inject;
import javax.inject.Named;
import javax.jws.WebParam
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam
import javax.ws.rs.GET
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.Consts
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value

import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.AuthenticationManager
import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.core.resources.RenderResource;
import net.opentechnology.triki.schema.Dcterms;
import net.opentechnology.triki.schema.Rdfbase;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor;

@Log4j
@Path("/auth")
public class AuthenticateResource extends RenderResource {

	public static final String SESSION_PERSON = "person";
	public static final String SESSION_ID = "id";
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Value('${client_id}')
	private String clientId;
	
	@Value('${indie_auth_redirect}')
	private String redirectUri;
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private final AuthenticationManager authMgr;
	
	@Inject
	private final Utilities utils;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void login(@Context HttpServletResponse resp, @Context HttpServletRequest req,
			MultivaluedMap<String, String> formParams,
			@FormParam("action") String action,
			@FormParam("triki:login") String login,
			@FormParam("triki:password") String password) throws ServletException, IOException {
		HttpSession session = req.getSession();
		try {
			checkKnownAndForward(session, resp){ ->
				authMgr.authenticate(login, password);
			}
		} catch (AuthenticationException e) {
			logger.warn(login + "/" + password + " failed to authenticate");
			resp.sendRedirect("/login");
		}
	}
	
	@GET
	@Path("/logoff")	
	public String logoff(@Context HttpServletResponse resp, @Context HttpServletRequest req, @QueryParam("action") String action)
	{
		HttpSession session = req.getSession();
		session.removeAttribute(SESSION_PERSON);
		session.removeAttribute(SESSION_ID);
		
		String url =  propStore.getPrivateUrl();
		String content = renderContent(url);
		
		return content;
	}

	private setKnownPersonSession(HttpSession session, Resource person) {
		session.setAttribute(SESSION_PERSON, person)
	}
	
	private setAuthenticatedPersonLogin(HttpSession session, String id) {
		session.setAttribute(SESSION_ID, id)
	}
	
	@Path("indie")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response content(@Context HttpServletResponse resp,@Context HttpServletRequest req,
			@QueryParam("code") String code,
			@QueryParam("me") String me)
		throws ServletException, IOException, URISyntaxException
	{	
		HttpSession session = req.getSession();
		Calendar timestampCal = Calendar.getInstance();
		timestampCal.setTime(new Date());
		
		logger.info("${me} has tried to login...");
		
		HttpPost poster = new HttpPost("https://indieauth.com/auth");
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("code", code));
		form.add(new BasicNameValuePair("client_id", clientId));
		form.add(new BasicNameValuePair("redirect_uri", redirectUri));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
		poster.setEntity(entity);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = httpclient.execute(poster);
		
		if(response.getStatusLine().getStatusCode() == Response.Status.OK.code)
		{
			logger.info("${me} successfully authenticated by indieauth");
			try {
				checkKnownAndForward(session, resp){ ->
					authMgr.authenticateById(me)
				}
			} catch (AuthenticationException e) {
				logger.info("Unknown person ${me} but is authenticated")
				setAuthenticatedPersonLogin(session, me);
				resp.sendRedirect("/resource/home");
			}
		}
		else
		{
			logger.warn("${me} not authenticated.");
			resp.sendRedirect("/resource/login");
		}
	}

	private checkKnownAndForward(HttpSession session, HttpServletResponse resp, Closure authMethod) {
		Resource person = authMethod();
		String id = person.getProperty(Dcterms.title).getString();
		setKnownPersonSession(session, person);
		Resource home = person.getPropertyResourceValue(Triki.home);
		if(session.getAttribute("origUrl") != null){
			logger.info("Redirecting ${id} to original URL");
			resp.sendRedirect(session.getAttribute("origUrl"));
		}
		else if(home != null)
		{
			String homeUrl = home.getURI().toString()
			logger.info("Redirecting ${id} to home page ${homeUrl}")
			resp.sendRedirect(utils.makeUrlPublic(homeUrl));
		}
		else
		{
			logger.info("Redirecting ${id} to home page")
			resp.sendRedirect("/");
		}
		
	}
	
}
