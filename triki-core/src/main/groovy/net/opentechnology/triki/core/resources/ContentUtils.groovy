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

package net.opentechnology.triki.core.resources

import java.io.InputStream;

import javax.inject.Inject
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.StreamingOutput;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.dto.ContentDto;
import net.opentechnology.triki.core.dto.MediaTypeDto;

import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils

class ContentUtils {
	
	@Inject
	private ApplicationContext appCtx;
	
	@Inject
	private MediaTypeDto mediaDto;
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private ContentDto contentDto;
	
	// Check module, then parent then file system
	public String getClasspathTextContent(String id) throws ResourceException {
		try {
			String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
			Resource res = appCtx.getResource(suffix + File.separator + id)
			if(appCtx.getResource(suffix + File.separator + id).exists()){
				InputStream textStream = appCtx.getResource(suffix + File.separator + id).getInputStream();
				String content = IOUtils.toString(textStream);
				return content;
			}
			else if(appCtx.getParent() != null && appCtx.getParent().getResource(suffix + File.separator + id).exists()){
				InputStream textStream = appCtx.getParent().getResource(suffix + File.separator + id).getInputStream();
				String content = IOUtils.toString(textStream);
				return content;
			}
			else {
				if(contentExists(id)){
					InputStream fileIn = new FileInputStream(propStore.getContentDir() + File.separator + suffix + File.separator + id);
					String content = IOUtils.toString(fileIn);
					return content
				}
				else {
					throw new Exception("File ${id} does not exist.")
				}
			}
		}
		catch (Exception e){
			throw new ResourceException("Could not read content due to " + e.getMessage());
		}
	}
	
	public boolean contentExists(String id)
	{
		String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
		return new File(propStore.getContentDir() + File.separator	+ suffix + File.separator + id).exists()
	}
	
	public String writeStream(String id, InputStream inStream) throws ResourceException {
		String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
		File contentFile = new File(propStore.getContentDir() + File.separator
						+ suffix + File.separator + id);
		try {
			FileOutputStream outStream = new FileOutputStream(contentFile)
			IOUtils.copy(inStream, outStream)
			outStream.close()
		} catch (IOException e) {
			throw new ResourceException("Could not write content due to " + e.getMessage());
		}
	}
	
	public OutputStream getWriteStream(String id) throws ResourceException {
		String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
		File contentFile = new File(propStore.getContentDir() + File.separator
						+ suffix + File.separator + id);
		try {
			return new FileOutputStream(contentFile)
		} catch (IOException e) {
			throw new ResourceException("Could not write content due to " + e.getMessage());
		}
	}
	
	public String writeContent(String id, String content) throws ResourceException {
		String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
		File contentFile = new File(propStore.getContentDir() + File.separator
						+ suffix + File.separator + id);
		try {
			 FileUtils.write(contentFile, content);
			return content;
		} catch (IOException e) {
			throw new ResourceException("Could not read content due to " + e.getMessage());
		}
	}
	
	/*
	 * Try local module, try parent and then try content directory
	 */
	public InputStream getInputStream(String id) throws IOException
	{
		try {
			String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
			Resource res = appCtx.getResource(suffix + File.separator + id)
			if(appCtx.getResource(suffix + File.separator + id).exists()){
				return appCtx.getResource(suffix + File.separator + id).getInputStream()
			}
			else if(appCtx.getParent() != null && appCtx.getParent().getResource(suffix + File.separator + id).exists()){
				return appCtx.getParent().getResource(suffix + File.separator + id).getInputStream()
			}
			else {
				if(contentExists(id)){
					return new FileInputStream(propStore.getContentDir() + File.separator + suffix + File.separator + id);
				}
				else {
					throw new Exception("File ${id} does not exist.")
				}
			}
		}
		catch (Exception e){
			throw new ResourceException("Could not read content due to " + e.getMessage());
		}
		
	}
	
	public String getUrlForId(String id)
	{
		String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
		String url = propStore.getPrivateUrl() + suffix + "/" + id
		return url;
	}
	
	public String getTemplateString(String id)
	{
		InputStream input = getInputStream(id)
		StringWriter writer = new StringWriter();
		IOUtils.copy(input, writer, "UTF-8");
		String templateStr = writer.toString();
		return templateStr
	}
	
	public void copyStream(InputStream input, OutputStream output) throws IOException
	{
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytesRead);
		}
	}
	
	public StreamingOutput getClasspathByteContent(String filename) throws ResourceException {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				InputStream dataStream = getInputStream(filename)
				copyStream(dataStream, output);
			}
			
			private InputStream getInputStream(String id) throws IOException
			{
				String suffix = id.replaceFirst("^.*\\.", "").toLowerCase();
				try {
					return appCtx.getParent().getResource(suffix + File.separator + id).getInputStream()
				}
				catch (Exception e) {
					try {
						return new FileInputStream(propStore.getContentDir() + File.separator + suffix + File.separator + id);
					}
					catch (Exception e2)
					{
						throw new ResourceException("Could not read content due to " + e2.getMessage());
					}
				}
			}
			
			public void copyStream(InputStream input, OutputStream output) throws IOException
			{
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buffer)) != -1)
				{
					output.write(buffer, 0, bytesRead);
				}
			}
		};
	}
	
}
