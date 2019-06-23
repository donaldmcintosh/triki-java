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


import org.apache.camel.CamelContext
import org.apache.camel.component.seda.SedaEndpoint
import org.apache.jena.rdf.model.Model
import org.springframework.beans.factory.annotation.Qualifier
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Component

import javax.inject.Inject;
import javax.ws.rs.core.Form

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.TrikiClient
import net.opentechnology.triki.core.boot.Utilities
import groovy.util.logging.Log4j

@Log4j
@Component
class TrikiHelper {
	
	@Inject @Qualifier("siteModel")
	private Model siteModel;
	
	@Inject
	TrikiClient client

	@Inject
	private RetrofitTrikiClient retrofitTrikiClient
	
	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private TestRoutes testRoutes;
	
	@Inject
	private MsgCollector msgCollector;
	
	@Inject
	private CachedPropertyStore propStore;
	
	def addTestRoutes()
	{
		camelCtx.addRoutes(testRoutes)
	}
	
	def saveContent(String filename)
	{
		client.saveContent(filename);
	}
	
	def saveResource(Map details)
	{
		client.saveResource(details);
	}
	
	def addNode(Map details)
	{
		client.addNode(details);
	}
	
	def addContent(List files)
	{
		client.addContent(files);
	}
	
	def saveGraph(def filename)
	{
		Utilities.saveSite(siteModel, filename);
	}
	
	def postNote(Form form)
	{
		client.postNote(form);
	}
	
	def postEmail(Form form)
	{
		client.postEmail(form);
	}
	
	def getContentAsString(String url)
	{
		client.getContentAsString(url)
	}

	def editContentAsString(String url)
	{
		client.editContentAsString(url)
	}
	
	def saveContentAsString(String url, String content)
	{
		client.saveContentAsString(url, content)
	}
	
	def validateContentAsString(String url, String content)
	{
		client.validateContentAsString(url, content)
	}
	
	def getResponse(String url)
	{
		client.getResponse(url)
	}
	
	def getResource(String path)
	{
		client.getResource(path)
	}

	def getImage(String path)
	{
		client.getImage(path)
	}
	
	def editResource(String path)
	{
		client.editResource(path)
	}

	def login(String username, String password)
	{
		client.login(username, password)
	}
	
	def logoff()
	{
		client.logoff()
	}

	def cloneResource(String path)
	{
		client.cloneResource(path)
	}
	
	def deleteResource(String path)
	{
		client.deleteResource(path)
	}
		
	def getTwitterMessages()
	{
		msgCollector.twitterMsgs
	}
	
	def getEmailMessages()
	{
		msgCollector.emailMsgs
	}
	
	def waitSedaQueueEmpty(String queue)
	{
		while(checkSedaQueueSize(queue) != 0)
		{
			log.info("Sleeping for a second")
			Thread.sleep(1000);
		}
	}
	
	def checkSedaQueueSize(String queue)
	{
		SedaEndpoint sedaq = camelCtx.getEndpoint(queue);
		return sedaq.exchanges.size();
	}
	
	public void checkContentFilesExist(List files)
	{	
		files.each { String file ->
			String dir = file.replaceAll(".*\\.", "").toLowerCase()
			File contentDir = new File(propStore.getContentDir() + File.separator + dir);
			assert new File(contentDir, file).exists()
		}
	}
	
	public void checkContentDirExist(String suffix)
	{
		File dirFile = new File(propStore.getContentDir() + File.separator + suffix);
		assert dirFile.exists()
	}
	
	public void deleteDirectoriesAfter()
	{
		if(propStore.getContentDir() != null)
		{
			FileUtils.deleteDirectory(new File(propStore.getContentDir()))
		}
	}
	
}
