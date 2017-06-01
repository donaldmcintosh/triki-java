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

package net.opentechnology.triki.image.module;

import java.awt.image.BufferedImage
import java.io.File;
import java.io.IOException;
import java.io.InputStream
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO
import javax.inject.Inject
import javax.servlet.http.HttpSession
import org.apache.commons.io.IOUtils

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF;
import org.imgscalr.Scalr
import org.springframework.beans.factory.annotation.Qualifier

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import groovy.util.logging.Log4j
import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule
import net.opentechnology.triki.core.dto.GroupDto
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.core.dto.TypeDto
import net.opentechnology.triki.core.dto.SettingDto.Settings
import net.opentechnology.triki.core.resources.ContentUtils
import net.opentechnology.triki.modules.ContentSaver
import net.opentechnology.triki.schema.Exif
import net.opentechnology.triki.schema.Rdfbase;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.schema.Dcterms

@Log4j
public class ImageContentSaver implements ContentSaver {

	private static final int MAX_WIDTH = 1000

	private static final int MAX_WIDTH_THUMB = 125
	
	@Inject
	private ContentUtils contentUtils;
	
	@Inject @Qualifier("siteModel")
	private Model siteModel;
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private SettingDto settingDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private GroupDto groupDto;
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private ResourceDto resourceDto;
	
	@Inject
	private HttpSession session;
	
	public void init(){
		coreModule.registerContentSaver("jpg", this);
	}

	public void saveContent(String filename, InputStream input, List<String> msgs, List<String> errors, String access) {
		saveImages(filename, input, msgs, errors, access)
	}
	
	private void saveImages(String filename, InputStream input, List<String> msgs, List<String> errors, String access)
	{
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(input, baos);
			byte[] bytes = baos.toByteArray();
			BufferedImage img = ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(bytes)));
			
			BufferedImage webImg = createWeb(img);
			def webName = getWebfileName(filename);
			OutputStream webFileOut = contentUtils.getWriteStream(webName);
			ImageIO.write(webImg, "jpg", webFileOut);
			
			BufferedImage thumbImg = createThumbnail(img);
			def thumbName = getThumbfileName(filename);
			OutputStream thumbFileOut = contentUtils.getWriteStream(thumbName);
			ImageIO.write(thumbImg, "jpg", thumbFileOut);
			
			addContentResource(webName, thumbName, new BufferedInputStream(new ByteArrayInputStream(bytes)), errors, access)
			msgs << "Created resource /content/${webName}"
			msgs << "Created resource /content/${thumbName}"
			msgs << "Created resource /image/${webName}"
		} catch (IOException e) {
			errors << "Problems saving image ${filename}: " + e.getMessage()
		}
	}

	private String getThumbfileName(String filename){
		String prefix = filename.replaceFirst("\\..*", "");
		String thumbFilename = prefix + "-thumb.jpg";
		return thumbFilename;
	}
	
	private String getWebfileName(String filename){
		String prefix = filename.replaceFirst("\\..*", "");
		String thumbFilename = prefix + "-web.jpg";
		return thumbFilename;
	}
	
	private BufferedImage createWeb(BufferedImage img) {
		if(img.width > MAX_WIDTH)
			return Scalr.resize(img, Scalr.Method.SPEED, MAX_WIDTH, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER);
		else
			return img
	}

	private BufferedImage createThumbnail(BufferedImage img) {
		if(img.width > MAX_WIDTH_THUMB)
			return Scalr.resize(img, Scalr.Method.SPEED, MAX_WIDTH_THUMB, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER);
		else
			return img
	}
	
	private void addContentResource(String webName, String thumbName, BufferedInputStream inStream, List<String> errors, String access) {
		try {
			Resource contentResource
			Metadata metadata = ImageMetadataReader.readMetadata(inStream, true);
			Iterable<Directory> dirs = metadata.getDirectories();
			Resource thumbRes = addThumbnailResource(siteModel, thumbName, access);
			Resource webResource = addResource(siteModel, webName, thumbRes, errors, access);
			for(Directory metadir: dirs){
				addMetadata(webResource, metadir, errors);
			}
			if(!webResource.hasProperty(Dcterms.created)){
				resourceDto.addCreatedNow(webResource)
			}
			addImageResource(webName, webResource, access);

		} catch (ImageProcessingException e) {
			log.error("Problems processing " + webName, e);
		} catch (IOException e) {
			log.error("Problems accessing " + webName, e);
		}
	}
	
	private Resource addResource(Model model, String webName, Resource thumbRes, List<String> errors, String access) {	
		try {
			String resname = "content/" + webName;
			Resource contentResource = model.createResource(propStore.privateUrl +  resname);
			contentResource.addProperty(RDF.type, Triki.Content)
				
			contentResource.addProperty(Triki.restricted, groupDto.getGroup(access));
			contentResource.addProperty(DCTerms.title, webName);
			contentResource.addProperty(Triki.thumbimg, thumbRes);
			resourceDto.addCreator(session, contentResource);
			
			return contentResource
		}
		catch(MetadataException e){
			errors << "Problems reading meta data: " + e.getMessage();
		}
	}
	
	private Resource addMetadata(Resource contentResource, Directory metadir, List<String> errors) {
		try {
			if(metadir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) != null){
				Date createdDate = metadir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				Calendar timestampCal = Calendar.getInstance();
				timestampCal.setTime(createdDate);
				Literal timestampLiteral = siteModel.createTypedLiteral(timestampCal);
				contentResource.addProperty(Dcterms.created, timestampLiteral);
				contentResource.addProperty(Exif.width, Integer.toString(metadir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)));
				contentResource.addProperty(Exif.height, Integer.toString(metadir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)));
			}
		}
		catch(MetadataException e){
			errors << "Problems reading meta data: " + e.getMessage();
		}
	}
	
	private Resource addThumbnailResource(Model model, String thumbName, String access) {
		String resname = "content/" + thumbName;
		Resource thumbnail = siteModel.createResource(propStore.getPrivateUrl() + resname, Triki.Content);
		thumbnail.addProperty(DCTerms.title, thumbName);
		thumbnail.addProperty(Triki.restricted, groupDto.getGroup(access));
		resourceDto.addCreator(session, thumbnail);
		
		return thumbnail;
	}
	
	private void addImageResource(String webName, Resource contentResource, String access)
	{
		String resname = "image/" + webName;
		Resource imageType = typeDto.getType("image")
		Resource image = siteModel.createResource(propStore.getPrivateUrl() + resname, imageType);
		image.addProperty(DCTerms.title, "Image ${webName}");
		image.addProperty(Triki.restricted, groupDto.getGroup(access));
		resourceDto.addCreator(session, image)
		resourceDto.addCreated(image, contentResource.getRequiredProperty(Dcterms.created).getObject().asLiteral());
		image.addProperty(Triki.content, contentResource);
	}

}
