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

package net.opentechnology.triki.core.boot

import java.io.File
import java.io.InputStream;
import java.nio.charset.StandardCharsets

import javax.inject.Inject
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response
import javax.xml.bind.attachment.AttachmentMarshaller;

import org.apache.commons.configuration.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext;
import org.apache.commons.io.IOUtils
import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.jaxrs.ext.multipart.Attachment
import org.apache.cxf.jaxrs.ext.multipart.AttachmentBuilder
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody

import net.opentechnology.triki.core.resources.ContentResource
import net.opentechnology.triki.core.resources.ResourceException;
import net.opentechnology.triki.core.resources.SearchResult
import net.opentechnology.triki.core.template.StringTemplateValidator;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider


import javax.ws.rs.core.Form
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.NewCookie

class TrikiClient {

	private static final String SESSION_COOKIE = "JSESSIONID"
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private ApplicationContext appCtx;
	
	private Client client;
	private String sessionId;
	
	public WebTarget initTarget(String path)
	{
		if(client == null)
		{
			client = ClientBuilder.newClient();
			client.register(JacksonJaxbJsonProvider.class)
			Response response = client.target(propStore.getPrivateUrl() + "login").request(MediaType.TEXT_HTML).get(Response.class)
			response.cookies.find { it.key == SESSION_COOKIE}.each { 
				 sessionId = it.value.value
			};
		}
		WebTarget target = client.target(propStore.getPrivateUrl()).path(path);
		return target
	}
	
	def String getContentAsString(String resource)
	{
		def contentPath = ContentResource.CONTENT_PATH + resource
		def target = initTarget(contentPath)
		String response = target.request(MediaType.TEXT_PLAIN).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}
	
	def String editContentAsString(String resource)
	{
		def contentPath = ContentResource.CONTENT_PATH + "/" + resource
		def target = initTarget(contentPath)
		String response = target.queryParam("action", "edit").request(MediaType.TEXT_PLAIN).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}
	
	def String saveContentAsString(String resource, String content)
	{
		def contentPath = ContentResource.CONTENT_PATH + "/" + resource
		def target = initTarget(contentPath)
		Form form = new Form();
		form.param("content", content)
		form.param("action", "save")
		String response = target.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
			.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class)
		return response;
	}
	
	def String validateContentAsString(String resource, String content)
	{
		def contentPath = ContentResource.CONTENT_PATH + "/" + resource
		def target = initTarget(contentPath)
		Form form = new Form();
		form.param("content", content)
		form.param("action", "validate")
		String response = target.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
			.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class)
		return response;
	}
	
	def String login(String username, String password)
	{
		def path = "/auth" 
		def target = initTarget(path)
		Form form = new Form();
		form.param("action", "login")
		form.param("triki:login", username)
		form.param("triki:password", password)
		Response response = target.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
			.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class)

		String redirect = response.getHeaderString("Location")
		URL redir = new URL(redirect)
		def redirTarget = initTarget(redir.path)
		
		String reply = redirTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return reply
	}
	
	def String logoff()
	{
		def contentPath = "/auth/logoff"
		def target = initTarget(contentPath)
		String response = target.request().cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		
		return response
	}
	
	def Response getResponse(String resource)
	{
		def contentPath = ContentResource.CONTENT_PATH + resource
		def target = initTarget(contentPath)
		Response response = target.request().cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(Response.class);
		return response;
	}
	
	def String getResource(String path)
	{
		def target = initTarget(path)
		Response response = target.request(MediaType.TEXT_HTML).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(Response.class);
		
		String redirect = response.getHeaderString("Location")
		if(redirect != null)
		{
			URL redir = new URL(redirect)
			def redirTarget = initTarget(redir.path)
			
			String reply = redirTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
			return reply;
		}
		else
		{
			return response.readEntity(String.class)
		}
	}
	
	def String getImage(String path)
	{
		def target = initTarget(path)
		String response = target.request("image/jpg").cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}
	
	def String editResource(String path)
	{
		def target = initTarget(path)
		String response = target.queryParam("action", "edit").request(MediaType.TEXT_HTML).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}

	def String cloneResource(String path)
	{
		def target = initTarget(path)
		String response = target.queryParam("action", "clone").request(MediaType.TEXT_HTML).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}

	def String deleteResource(String path)
	{
		def target = initTarget(path)
		String response = target.queryParam("action", "delete").request(MediaType.TEXT_HTML).cookie(new NewCookie(SESSION_COOKIE, sessionId)).get(String.class);
		return response;
	}
			
	def List<SearchResult> search(String suffix, String term)
	{
		def target = initTarget("/search" + suffix)
		List<SearchResult> results = target.queryParam("term", term).request().cookie(new NewCookie(SESSION_COOKIE, sessionId)).accept(MediaType.APPLICATION_JSON).get(List.class)
		return results;
	}
	
//	
//	def Response postNote(Form form)
//	{
//		def target = initTarget(PublishResource.PUBLISH + "/" + PublishResource.NOTE)
//		Response response = target.request(MediaType.MULTIPART_FORM_DATA)
//				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);
//		return response;
//	}
//	
//	def Response postEmail(Form form)
//	{
//		def target = initTarget(PublishResource.PUBLISH + "/" + PublishResource.EMAIL)
//		Response response = target.request(MediaType.MULTIPART_FORM_DATA)
//				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);
//		return response;
//	}
//	
	def Response saveContent(def filename)
	{
		String suffix = filename.replaceFirst("^.*\\.", "").toLowerCase();
		InputStream instr = appCtx.getResource(suffix + File.separator + filename).getInputStream();
		def content = IOUtils.toString(instr);
		
		def contentTarget = initTarget(ContentResource.CONTENT_PATH + "/" + filename)
		Form form = new Form();
		form.param("content", content)
		Response response = contentTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class)
				
		return response;
	}
	
	def Response saveResource(Map details)
	{
		HashMap modifyMap = [:]
		modifyMap.putAll(details)
		Form form = new Form();
		def path = modifyMap.remove("path")
		modifyMap.each { entry ->
			form.param(entry.key, entry.value)
		}
		
		def contentTarget = initTarget(path)
		Response response = contentTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class)
		
		return response;
		
	}
	
	def String addNode(Map details)
	{
		List<Attachment> attachments = new ArrayList<>();
		details.each { entry ->
			Attachment attachment = new AttachmentBuilder()
				.id(entry.key)
				.object(entry.value)
				.contentDisposition(new ContentDisposition("form-data; name=\"${entry.key}\"")).build();
//			val content1 = new ContentDisposition(s"filename=myfile.xml")
//			val xml = "the xml that you might want to upload"
//			val attachments1 = new Attachment("root", new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8)), content1)
			
			attachments << attachment
		}
		
		def contentTarget = initTarget("/graph")
		String response = contentTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
				.post(Entity.entity(attachments, MediaType.MULTIPART_FORM_DATA), String.class)
		
		return response;
		
	}
	
	def String addContent(List files)
	{
		List<Attachment> attachments = new ArrayList<>();
		files.each { file ->
			ContentDisposition content = new ContentDisposition("form-data; name=uploadfiles; filename=${file};")
			InputStream fileIn = getClasspathTextContent(file)
			Attachment attachment = new Attachment("root", fileIn, content)
			
			attachments << attachment
		}
		
		// Default access
		ContentDisposition content = new ContentDisposition("form-data; name=access;")
		InputStream inStr = new ByteArrayInputStream("private".getBytes(StandardCharsets.UTF_8))
		Attachment accessAttach = new Attachment("root", inStr, content)
		attachments << accessAttach
		
		def contentTarget = initTarget("/content")
		String response = contentTarget.request().cookie(new NewCookie(SESSION_COOKIE, sessionId))
				.post(Entity.entity(attachments, MediaType.MULTIPART_FORM_DATA), String.class)
		
		return response;
		
	}
	
	private InputStream getClasspathTextContent(String id) throws ResourceException {
		try {
			InputStream textStream = appCtx.getResource("upload" + File.separator + id).getInputStream();
			return IOUtils.toBufferedInputStream(textStream);
		} catch (IOException e) {
			throw new ResourceException("Could not read content due to " + e.getMessage());
		}
	}
}
