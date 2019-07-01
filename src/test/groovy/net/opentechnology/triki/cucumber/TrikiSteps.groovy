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

package net.opentechnology.triki.cucumber

import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.jena.rdf.model.Model;
import org.eclipse.jetty.server.Server;
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration

import org.apache.commons.io.FileUtils
import cucumber.api.java.After
import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.And
import gherkin.formatter.model.DataTableRow
import groovy.util.logging.Log4j
import java.lang.reflect.Field
import java.text.SimpleDateFormat;

import javax.ws.rs.core.Form
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import net.opentechnology.triki.core.boot.TrikiMain
import net.opentechnology.triki.core.resources.SearchResult
import net.opentechnology.triki.share.async.SendTwitter
import net.opentechnology.triki.share.async.SendEmail
import net.opentechnology.triki.core.renderer.DateRenderer

import javax.inject.Inject

import cucumber.api.java.Before

@Log4j
class TrikiSteps {
	
	TrikiMain server = new TrikiMain();
	private TrikiHelper helper;
	def errorMsg;
	String responsePage;
	
	Response response;
	
	@Given("initialise triki")
	public def "initialise triki with props"()
	{	
		server.initialise()

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(TrikiClientConfig.class)
		ctx.setParent(server.getCtx())
		ctx.refresh();

		helper = ctx.getBean(TrikiHelper.class, "trikiHelper")
		helper.addTestRoutes();
	}
	
	@Given("start triki with content path (.+) and port (\\d+)")
	public def "start triki with root dir"(def path, def port)
	{
		setSystemPropertiesStartup(path, port);
		server.start();
		ApplicationContext ctx = server.getCtx();
		helper = ctx.getBean(TrikiHelper.class, "trikiHelper")
	}

	private setSystemPropertiesStartup(path, port) {
		System.setProperty("port", port);
		System.setProperty("content_dir", path)
		System.setProperty("mode", "test")
	}
	
	@And("start triki with no content path")
	public def "start triki"()
	{
		try {
			System.clearProperty("port");
			System.clearProperty("content_dir")
			log.info("Starting server")
			server.start();
			assert false;
		}
		catch(RuntimeException e)
		{
			errorMsg = e.getMessage()
		}
	}
	
	@And("expect error message \"(.+)\"")
	public def "expect error message"(msg)
	{
		assert errorMsg == msg;
	}
	
	@And("create a new note with following parameters")
	public def "publish note"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		Form form = new Form();
		form.param("triki:note", values."Note")
		form.param("triki:subject", values."Subject")
		form.param("triki:twitter", values."Twitter")
		response = helper.postNote(form);
	}
	
	@And("create a new email with following parameters")
	public def "send emails"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		Form form = new Form();
		form.param("triki:note", values."Note")
		form.param("triki:subject", values."Subject")
		form.param("triki:email", values."Email1")
		form.param("triki:email", values."Email2")
		response = helper.postEmail(form);
	}
	
	@And("search for term (.+) and expect following response")
	public def "search for term title and expect following response"(def term, DataTable matches)
	{
		List<SearchResult> results = helper.client.search("", term)
		checkSearchResults(matches, results)
	}
	
	@And("search properties for term (.+) and expect following response")
	public def "search props for term title and expect following response"(def term, DataTable matches)
	{
		List<SearchResult> results = helper.client.search("/properties", term)
		checkSearchResults(matches, results)
	}
	
	@And("search local prefixes for term (.+) and expect following response")
	public def "search prefixes for term title and expect following response"(def term, DataTable matches)
	{
		List<SearchResult> results = helper.client.search("/prefixes", term)
		checkSearchResults(matches, results)
	}

	private checkSearchResults(DataTable matches, List results) {
		List<Map<String, String>> expectedResults = matches.asMaps(String.class, String.class)
		assert expectedResults.size() == results.size()
		expectedResults.each { expected ->
			boolean foundit = false;
			results.each { found ->
				if(found.value == expected."value" 
					&& found.id == expected."id"
					&& found.urlencoded == expected."urlencoded")
				{
					foundit = true
				}
			}
			assert foundit
		}
	}
	
	@And("add intercept routes")
	public def "add intercept routes"()
	{
		SendTwitter.metaClass.interceptor = { ->
			// Intercept must be defined before endpoint it is acting against
			interceptSendToEndpoint("twitter://timeline/user.*")
				.skipSendToOriginalEndpoint()
				.to("direct:msgCollectorTwitter");
		}
		
		SendEmail.metaClass.interceptor = { ->
			interceptSendToEndpoint("smtps://.*")
				.skipSendToOriginalEndpoint()
				.to("direct:msgCollectorEmail");
		}
	}
	
	@And("check HTTP response is (\\d+)")
	public def "check status response"(int status)
	{
		assert status == response.getStatus()
	}
	
	@And("join")
	public def "join"()
	{
		server.join()
	}
	
	@And("check URI returned is valid")
	public def "check uri response"()
	{
		log.info("Location is ${response.location}")
		assert response.location != null
	}
	
	@And("check contents of created URL contain")
	public def "check note contents"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		String content = helper.getContentAsString(response.location.toString());
		values.each { entry ->
			assert content.contains(entry.value);
		}
	}
	
	@And("get content (.+) and check contains")
	public def "get content"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.getContentAsString(path);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("edit content (.+) and check contains")
	public def "edit content"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.editContentAsString(path);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("update content (.+) with content \"(.+)\" and check contains")
	public def "update content"(String path, String newContent, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.saveContentAsString(path, newContent);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("validate content (.+) with content \"(.+)\" and check contains")
	public def "validate content"(String path, String newContent, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.validateContentAsString(path, newContent);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("get content (.+) and check content type is (.+)")
	public def "get content type"(String path, String contentType)
	{
		Response response = helper.getResponse(path);
		def contentTypeActual = response.headers."content-type".first()
		assert contentTypeActual == contentType
	}
	
	@And("get resource (.+) and check contains")
	public def "get resource"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.getResource(path);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("get resource (.+) and check has todays date")
	public def "contains todays date"(String path)
	{
		Date now = new Date()
		String webPattern = DateRenderer.DATE_RENDERER_FORMAT;
		SimpleDateFormat formatter = new SimpleDateFormat(webPattern);
		String today = formatter.format(now); 
		String content = helper.getResource(path);
		assert content.contains(today)
	}
	
	@And("edit resource (.+) and check does not contain")
	public def "get resource not contains"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.editResource(path);
		values.each { entry ->
			assert !content.contains(entry)
		}
	}
	
	@And("get resource (.+) and check content is ordered")
	public def "get resource order"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.getResource(path);
		int lastIndex = 0
		int thisIndex = 0
		values.each { entry ->
			thisIndex = content.indexOf(entry)
			assert thisIndex > lastIndex
			lastIndex = thisIndex
		}
	}
	
	@And("edit resource (.+) and check content is ordered")
	public def "edit resource order"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.editResource(path);
		int lastIndex = 0
		int thisIndex = 0
		values.each { entry ->
			thisIndex = content.indexOf(entry)
			assert thisIndex > lastIndex
			lastIndex = thisIndex
		}
	}
	
	@And("get image (.+)")
	public def "get image"(String path)
	{
		String content = helper.getResource(path);
	}
	
	@And("edit resource (.+) and check contains")
	public def "edit resource"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.editResource(path);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("clone resource (.+) and check does not contains")
	public def "clone resource"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.cloneResource(path);
		values.each { entry ->
			assert !content.contains(entry)
		}
	}
	
	@And("delete resource (.+) and check response contains")
	public def "delete resource"(String path, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String content = helper.deleteResource(path);
		values.each { entry ->
			assert content.contains(entry)
		}
	}
	
	@And("add a content resource (.+)")
	public def "add a content resource content"(def filename)
	{
		helper.saveContent(filename);
	}
	
	@And("check twitter message contains")
	public def "check twitter message contains"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		String content = helper.getTwitterMessages()[0]
		values.each { entry ->
			assert content.contains(entry.value);
		}
	}
	
	@And("check email message contains")
	public def "check email message contains"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		String content = helper.getEmailMessages()[0]
		values.each { entry ->
			assert content.contains(entry.value);
		}
	}

	@And("add resource with following details")
	public def "add resource with following details"(DataTable inputs)
	{
		def values = inputs.asMap(String.class, String.class);
		helper.saveResource(values);
	}
	
	@And("update node with following details")
	public def "update node with following details"(DataTable inputs)
	{
		def values = [:]
		values << inputs.asMap(String.class, String.class);
		values."action" = "edit"
		responsePage = helper.addNode(values);
	}
	
	@And("add node with following details")
	public def "add node with following details"(DataTable inputs)
	{
		def values = [:] 
		values << inputs.asMap(String.class, String.class);
		values."action" = "add"
		responsePage = helper.addNode(values);
	}
	
	@And("add content from files")
	public def "add content from files"(DataTable inputs)
	{
		def values = inputs.asList(String.class);
		responsePage = helper.addContent(values);
		log.info(responsePage);
	}
	
	@And("check content exists")
	public def "check content exists"(DataTable inputs)
	{
		def values = inputs.asList(String.class);
		helper.checkContentFilesExist(values);
	}
	
	@And("check response page contains \"(.*)\"")
	public def "check result contains"(String term)
	{
		assert responsePage.contains(term)
	}
	
	@And("check response page contains all of")
	public def "check result contains all"(DataTable inputs)
	{
		def values = inputs.asList(String.class)
		values.each{ String value ->
			assert responsePage.contains(value)
		}
	}
	
	@And("check response page contains in order")
	public def "check result contains ordered"(DataTable inputs)
	{
		def values = inputs.asList(String.class)
		String thisIndex = 0
		String lastIndex = 0
		values.each{ String value ->
			thisIndex = responsePage.indexOf(value)
			assert thisIndex > lastIndex
			lastIndex = thisIndex
		}
	}
		
	@And("stop triki")
	public def "stop triki"()
	{
		server.stop();
	}
	
	@And("check sent (\\d+) twitter messages")
	public def "check got twitter messages"(int count)
	{
		helper.waitSedaQueueEmpty("seda:totwitter")
		assert helper.getTwitterMessages().size() == count
	}
	
	@And("check sent (\\d+) email messages")
	public def "check got email messages"(int count)
	{
		helper.waitSedaQueueEmpty("seda:toemail")
		assert helper.getEmailMessages().size() == count
	}
	
	@Given("create empty directory (.+)")
	public def createDir(String path)
	{
		assert newDirectory(path)
	}
	
	@And("check content dir (\\w+) exists")
	public def checkContentDirExists(String suffix)
	{
		helper.checkContentDirExist(suffix)
	}
	
	@And("login with (\\w+)/(\\w+) and check response contains")
	public def login(String user, String password, DataTable inputs)
	{
		def values = inputs.asList(String.class)
		responsePage = helper.login(user, password)
		values.each{ String value ->
			assert responsePage.contains(value)
		}
	}
	
	@And("logoff and check response contains")
	public def logff(DataTable inputs)
	{
		def values = inputs.asList(String.class)
		responsePage = helper.logoff()
		values.each{ String value ->
			assert responsePage.contains(value)
		}
	}
	
	@And("login with user (\\w+) password (\\w+)")
	public def loginSimple(String user, String password)
	{
		responsePage = helper.login(user, password)
	}
	
	@Given("save graph to (.+)")
	public def saveGraph(def filename)
	{
		helper.saveGraph(filename);
	}
	
	@After
	public void stopAndTidyUp()
	{
		server.stop()
		if(helper != null)
		{
			helper.deleteDirectoriesAfter()
		}
	}

	def newDirectory(String path)
	{
		File dir = new File(path)
		FileUtils.deleteDirectory(dir)
		if(!dir.exists()){
			dir.mkdirs()
		}
	}
	

}
