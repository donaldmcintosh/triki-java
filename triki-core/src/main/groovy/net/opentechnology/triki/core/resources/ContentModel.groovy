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

import java.util.Map

import javax.inject.Inject

import org.apache.commons.io.IOUtils

import org.apache.cxf.jaxrs.ext.multipart.Attachment
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.rdf.model.StmtIterator

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.core.dto.PrefixDto
import net.opentechnology.triki.core.dto.PropertyDto
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.resources.NodeFormModel.Action
import net.opentechnology.triki.schema.Triki;

import org.springframework.beans.factory.annotation.Qualifier
import org.apache.jena.vocabulary.DCTerms
import org.apache.commons.lang.StringEscapeUtils

import org.apache.commons.io.FilenameUtils
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode

@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ContentModel {

	@Inject @Qualifier("siteModel")
	protected Model siteModel;

	@Inject
	private Utilities utils;

	@Inject
	private PropertyDto propertyDto;

	@Inject
	private ResourceDto resourceDto;

	@Inject
	private PrefixDto prefixDto;

	@Inject
	private CachedPropertyStore propStore;
	
	public enum ContentAction  {
		view,
		edit,
		apply,
		validate,
		save
	}

	private ContentAction action;
	private List<ContentFile> contentFiles = new ArrayList<ContentFile>();
	private List<String> msgs = new ArrayList<>();
	private List<String> errors = new ArrayList<>();
	private String path;
	private String content;

	public addFormData(List<Attachment> attachments) {
		try {
			addContentFiles(attachments)
		}
		catch (FormValidationException fve){
			errors << fve.getMessage()
		}
	}
	
	public def addContentFiles(List<Attachment> attachments)
	{
		attachments.each { Attachment attachment ->
		def contentData = [:]
			ContentFile contentFile = new ContentFile()
			ContentDisposition content = attachment.getContentDisposition();
			def name = content.getParameter("name")
			def type = content.getType();
			if(type == "form-data" && name == "uploadfiles")
			{
				contentFile.filename = content.getFilename();
				if(!contentFile.filename?.trim()) throw new FormValidationException("A filename must be specified.")
				if((contentFile.filename ==~ /.*_.*/))
				{
					msgs << "Replaced underscores with dashes in filename ${contentFile.filename} as underscores are reserved character within Triki."
					contentFile.filename = contentFile.filename.replaceAll("_", "-")
				}
				contentFile.inputStream = attachment.getObject(InputStream.class);
				if(contentFile.inputStream == null) throw new FormValidationException("File ${contentFile.filename} is not accessible.")
				contentFile.suffix = FilenameUtils.getExtension(contentFile.filename).toLowerCase();
				if(!contentFile.suffix?.trim()) throw new FormValidationException("File must have a suffix")
				
				contentFiles << contentFile
			}
		}
	}
	
	public List<String> getMsgs() {
		return msgs;
	}

	public void setMsgs(List<String> msgs) {
		this.msgs = msgs;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public String getContent() {
		return content;
	}
	
	/* 
	 * We must prevent a textarea from showing up in the Content editor as it
	 * will confuse the HTML page, so we escape it.
	 */
	private String escapeContent(String unescaped)
	{
		return unescaped.replaceAll("textarea", "_textarea");
	}
	
	private String unEscapeContent(String unescaped)
	{
		return unescaped.replaceAll("_textarea", "textarea");
	}

	public String getPath() {
		return path;
	}
}

public class ContentFile {

	private String id;
	private String filename;
	private String suffix;
	private InputStream inputStream;
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}