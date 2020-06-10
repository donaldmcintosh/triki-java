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

package net.opentechnology.triki.image.loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import net.opentechnology.triki.core.model.ModelException;
import net.opentechnology.triki.schema.Exif;
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;

public class ImageImporter {
	
	String resourcePrefix = "/resource/";
	String imagePrefix = "/image/";
	
	Logger logger = Logger.getLogger(this.getClass());
	Model model = ModelFactory.createDefaultModel();
	private final String scanDir;
	private final String outDir;
	private final String baseUrl;
	private final String creator;
	private final String outFilename;
	private final String[] restricted;
	
	public ImageImporter(PropertiesConfiguration config){
		scanDir = config.getString("photos_scanDir");
		outDir = config.getString("photos_outDir");
		baseUrl = config.getString("photos_baseUrl");
		creator = config.getString("creator");
		outFilename = config.getString("photos_outFilename");
		restricted = config.getString("photos_restricted").split(" *, *");
	}
	
	public ImageImporter(Model model, String outDir, String scanDir, String url, String creator, String outFilename, String[] restricted){
		this.model = model;
		this.outDir = outDir;
		this.scanDir = scanDir;
		this.baseUrl = url;
		this.creator = creator;
		this.outFilename = outFilename;
		this.restricted = restricted;
	}

	public void importMetaData() {
		setPrefixes();
		Iterator<File> images = FileUtils.iterateFiles(new File(scanDir), new String[] {"jpg", "JPG"}, true);
		try {
			while(images.hasNext()){
				File imageFile = images.next();
				String path = imageFile.getParent().replaceFirst(scanDir, outDir);
				File pathDir = new File(path);
				if(!pathDir.exists()){
					pathDir.mkdirs();
				}
			
				File outThumbnail = new File(path, getThumbName(imageFile.getName()));
				File outWeb = new File(path, getWebName(imageFile.getName()));
	
				if(!outThumbnail.exists() || !outWeb.exists()){
					BufferedImage img = ImageIO.read(imageFile);
					if(!outThumbnail.exists()){
						logger.info("Creating " + outThumbnail.getAbsolutePath());
						BufferedImage thumbImg = createThumbnail(img);
						ImageIO.write(thumbImg, "jpg", outThumbnail);
					}
					
					if(!outWeb.exists()){
						logger.info("Creating " + outWeb.getAbsolutePath());
						BufferedImage webImg = createWeb(img);
						ImageIO.write(webImg, "jpg", outWeb);
					}
				}
				
				addImageResource(imageFile);
			}
			saveModel();
		} catch (ModelException e) {
			logger.error("Problems with RDF model " , e);
		} catch (IOException e) {
			logger.error("IO problems " + e);
		}
	}

	private Resource addThumbnailResource(String fileName) {
		String thumbName = getThumbName(fileName);
		logger.info("Adding " + thumbName);
		
		String resname = resourcePrefix + thumbName;
		Resource image = model.createResource(baseUrl + resname, FOAF.Image);
		image.addProperty(Triki.content, imagePrefix + thumbName);
		
		return image;
	}

	private void addImageResource(File imageFile) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
			Iterable<Directory> dirs = metadata.getDirectories();
			boolean resourceAdded = false;
			for(Directory metadir: dirs){
				//if(metadir.getName().equals("Exif SubIFD")){
					resourceAdded = addResource(model, imageFile, metadir);
				//}
				//printAllTags(metadir.getName(), metadir);
			}
			if(!resourceAdded){
				addSimpleResource(model, imageFile);
			}
		} catch (ImageProcessingException e) {
			logger.error("Problems processing file " + imageFile, e);
		} catch (IOException e) {
			logger.error("Problems accessing file " + imageFile, e);
		}
	}
	
	private void addSimpleResource(Model model, File jpg) {
		String webName = getWebName(jpg.getName());
		String thumbName = getThumbName(jpg.getName());
		
		logger.info("Adding " + webName);

		String resname = resourcePrefix + webName;
		Resource image = model.createResource(baseUrl + resname, FOAF.Image);

		image.addProperty(Triki.content, imagePrefix + webName);
		Resource thumbRes = addThumbnailResource(jpg.getName());
		image.addProperty(Triki.thumbimg, thumbRes);
		//image.addProperty(Triki.restricted, Triki.publicUrl);
	
	}

	private String getThumbfileName(File imgFile){
		String prefix = imgFile.getAbsolutePath().replaceFirst("\\..*", "");
		String thumbFilename = prefix + "_thumb.JPG";
		return thumbFilename;
	}
	
	private String getThumbName(String name){
		String prefix = name.replaceFirst("\\..*", "");
		String thumbFilename = prefix + "_thumb.JPG";
		return thumbFilename;
	}

	private String getWebName(String name){
		String prefix = name.replaceFirst("\\..*", "");
		String thumbFilename = prefix + "_web.JPG";
		return thumbFilename;
	}

	private String getWebfileName(File imgFile){
		String prefix = imgFile.getAbsolutePath().replaceFirst("\\..*", "");
		String thumbFilename = prefix + "_web.JPG";
		return thumbFilename;
	}
	
	private void resizeImage(File imageFile) throws IOException {
		if(imageFile.getName().matches(".*_web.*") || imageFile.getName().matches(".*_thumb.*")){
			logger.info("Skipping " + imageFile.getName());
			return;
		}
		
		String thumbFilename = getThumbfileName(imageFile);
		String webFilename = getWebfileName(imageFile);
		
		File thumbFile = new File(thumbFilename);
		File webFile = new File(webFilename);
		
		if(!webFile.exists() || !thumbFile.exists()){
			BufferedImage img = ImageIO.read(imageFile);
			if(!thumbFile.exists()){
				logger.info("Creating " + thumbFilename);
				BufferedImage thumbImg = createThumbnail(img);
				ImageIO.write(thumbImg, "jpg", thumbFile);
			}
			
			if(!webFile.exists()){
				logger.info("Creating " + webFilename);
				BufferedImage webImg = createWeb(img);
				ImageIO.write(webImg, "jpg", webFile);
			}
		}

	}

	private BufferedImage createWeb(BufferedImage img) {
		return Scalr.resize(img, Scalr.Method.SPEED, 2000, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER);
	}

	private BufferedImage createThumbnail(BufferedImage img) {
		return Scalr.resize(img, Scalr.Method.SPEED, 400, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER);
	}

	private void setPrefixes(){
		model.setNsPrefix("resource", baseUrl + resourcePrefix);
		model.setNsPrefix("image", baseUrl + imagePrefix);
		model.setNsPrefix("exif", Exif.NS);
		model.setNsPrefix("dc", DCTerms.NS);
		model.setNsPrefix("triki", Triki.NS);
		model.setNsPrefix("time", Time.NS);
		model.setNsPrefix("foaf", FOAF.NS);
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	}
	
	private void printAllTags(String name, Directory metadir){
		for(Tag tag: metadir.getTags()){
			logger.info(tag + " " + tag.getTagType() +
					" " + metadir.getDate(tag.getTagType()));
		}
	}

	private boolean addResource(Model model, File jpg, Directory metadir) {
		String webName = getWebName(jpg.getName());
		String thumbName = getThumbName(jpg.getName());
		
		//logger.info("Adding " + webName);
		try {
			String resname = resourcePrefix + webName;
			Resource image = model.createResource(baseUrl + resname, FOAF.Image);
			Date created = metadir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			
			if(created != null){
				Calendar timestampCal = Calendar.getInstance();
				timestampCal.setTime(created);
				Literal timestampLiteral = model.createTypedLiteral(timestampCal);
				image.addProperty(DCTerms.created, timestampLiteral);
			}
			else {
				logger.warn("No timestamp for " + webName);
			}
				
			for(String restrict: restricted){
				String prefix = restrict.replaceFirst(":.*$", "");
				String suffix = restrict.replaceFirst("^.*:", "");
				Resource restriction = model.createResource(model.getNsPrefixURI(prefix) + suffix);
				image.addProperty(Triki.restricted, restriction);
			}
			Resource creatorRes = model.createResource(creator, FOAF.Person);
			image.addProperty(DCTerms.creator, creatorRes);
			image.addProperty(DCTerms.title, jpg.getName());
			image.addProperty(Triki.content, imagePrefix + webName);
			Resource thumbRes = addThumbnailResource(jpg.getName());
			image.addProperty(Triki.thumbimg, thumbRes);	
			
			if(metadir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) != null){
				image.addProperty(Exif.dateTimeOriginal, metadir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL).toString());
				image.addProperty(Exif.width, Integer.toString(metadir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)));
				image.addProperty(Exif.height, Integer.toString(metadir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)));
			}
			
			if(created != null){
				indexDate(image, created);
			}
		}
		catch(MetadataException e){
			logger.error("Problems reading meta data for " + jpg, e);
		}
		
		return true;

	}

	private void indexDate(Resource image, Date created) {
		SimpleDateFormat monFormat = new SimpleDateFormat("MMMMM");
		String month = monFormat.format(created);
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		String year = yearFormat.format(created);
		
		String monthResName = resourcePrefix + year + month;
		Resource monthRes = model.createResource(baseUrl + monthResName, Time.Instant);
		monthRes.addLiteral(DCTerms.description, month + " " + year);
		monthRes.addLiteral(DCTerms.title, month + " " + year);
		monthRes.addProperty(Time.month, month);
		
		String yearResName = resourcePrefix + year;
		Resource yearRes = model.createResource(baseUrl + yearResName, Time.Instant);
		yearRes.addLiteral(DCTerms.description, year);
		yearRes.addLiteral(DCTerms.title, year);
		yearRes.addProperty(Time.year, year);
		
		for(String restrict: restricted){
			String prefix = restrict.replaceFirst(":.*$", "");
			String suffix = restrict.replaceFirst("^.*:", "");
			Resource restriction = model.createResource(model.getNsPrefixURI(prefix) + suffix);
			monthRes.addProperty(Triki.restricted, restriction);
			yearRes.addProperty(Triki.restricted, restriction);
		}
		
		monthRes.addProperty(Time.year, yearRes);
		image.addProperty(Time.month, monthRes);
	}

	private void saveModel() throws ModelException {
		File outFile = new File(outFilename);
		
		try {
			FileOutputStream fos = new FileOutputStream(outFile);
			model.write(fos, "TURTLE");
		} catch (FileNotFoundException e) {
			throw new ModelException("Could not save file due to " + e.getMessage());
		}
	}	
	
	public static void main(String[] args){
		ImageImporter ii;
		String propsFile = System.getProperty("propsPath");
		
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(propsFile);
			ii = new ImageImporter(config);
			ii.importMetaData();
		} catch (Exception e) {
			System.err.println(e);
		}
		
	}
}
