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

package net.opentechnology.triki.share.resources;

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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.dto.GroupDto
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.core.dto.TypeDto
import net.opentechnology.triki.schema.Dcterms;
import net.opentechnology.triki.schema.Rdfbase;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.share.async.SendEmail
import net.opentechnology.triki.share.module.ShareModule;
import net.opentechnology.triki.sparql.SparqlExecutor;

import net.opentechnology.triki.core.dto.SettingDto.Settings

@Log4j
@Path("/")
public class ShareResource {
	public static final String PUBLISH = "publish";
	public static final String NOTE = "note";
	public static final String EMAIL = "email";
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Inject
	private CamelContext camel;
	
	@Inject	@Qualifier("siteModel")
    private Model siteModel;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private GroupDto groupDto;
	
	@Inject
	private ResourceDto resourceDto;
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private HttpSession session;
	
	@POST
	@Path('/note')
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response publishNote(@Context HttpServletResponse resp, @Context HttpServletRequest req,
			MultivaluedMap<String, String> formParams,
			@FormParam("triki:note") String note, 
			@FormParam("triki:subject") String subject,
			@FormParam("triki:imgurl") String imgurl,
			@FormParam("triki:twitter") String twitter
			) throws ServletException, IOException, URISyntaxException
	{	
		String path;
		if(subject == null)
		{
			path = "note/" + UUID.randomUUID();
		}
		else
		{
			String shortLink = subject.replaceAll(" ", "-").toLowerCase();
			path = "note/" + shortLink;
		}
		
		Resource res = siteModel.createResource(propStore.getPrivateUrl() + path);
		res.addProperty(Rdfbase.type, typeDto.getType("note"));
		resourceDto.addCreator(session, res);
		resourceDto.addCreatedNow(res);
		res.addProperty(DCTerms.title, subject);
		res.addProperty(DCTerms.description, note);
		if(imgurl){
			res.addProperty(Triki.thumbimg, imgurl);
		}
		res.addProperty(Triki.restricted, groupDto.getGroup("public"));
		
		if(twitter != null && twitter.equalsIgnoreCase("yes"))
		{
			String tweet = note;
			ProducerTemplate template = camel.createProducerTemplate();
			template.sendBody("seda:totwitter", tweet);
		}
		
		return resp.sendRedirect("/");
	}
	
	@POST
	@Path('/email')
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response publishEmail(@Context HttpServletResponse resp, @Context HttpServletRequest req,
			MultivaluedMap<String, String> formParams,
			@FormParam("triki:note") String note,
			@FormParam("triki:reminder") String reminder,
			@FormParam("triki:subject") String subject,
			@FormParam("triki:email") String[] names
			) throws ServletException, IOException, URISyntaxException
	{	
		ProducerTemplate template = camel.createProducerTemplate();
		
		String originalNote = note;
		
		for(String name: names)
		{		
			Map<String, Object> map = new HashMap<String, Object>();
			String emailFrom = settingDto.getSetting(ShareModule.Settings.EMAILFROM.name())
			map.put("From", "${emailFrom}");
			map.put("Subject", subject);
			
			SparqlExecutor sparqler = new SparqlExecutor();
			String query = """
				PREFIX triki: <http://www.opentechnology.net/triki/0.1/> 
				PREFIX dc:    <http://purl.org/dc/terms/>
				PREFIX foaf:  <http://xmlns.com/foaf/0.1/>

				SELECT ?login ?password ?mbox
				 WHERE {  
					?friend triki:login?login .
					?friend triki:password ?password .
					?friend foaf:mbox ?mbox .
					?friend dc:title "${name}" .
				}"""
			
			sparqler.execute(siteModel, query) { QuerySolution soln ->
				RDFNode login = soln.get("login");
				RDFNode password = soln.get("password");
				RDFNode mbox = soln.get("mbox");
				
				if(reminder.equals("yes"))
					{
					String reminderText = "(Your login is ${login}/${password})"
					note = originalNote + "\n\n${reminderText}"
				}
					
				map.put("To", mbox);
				log.info("Sending email to ${name}");
				template.sendBodyAndHeaders("seda:toemail", note, map);
			}

		}
		
		return resp.sendRedirect("/");
	}

	protected String convertTweet(String note, String noteUrl) {
		if(note.length() < 119)
		{
			return note + " " + noteUrl;
		}
		else 
		{
			return note.substring(0, 140 - 21 - 6) + " ... " + noteUrl;
		}

	}
		
}
