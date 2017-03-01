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

import org.apache.commons.configuration.Configuration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject
import javax.inject.Named

import net.opentechnology.triki.core.model.ModelException;

class Utilities {
	
	@Inject
	private CachedPropertyStore propStore; 
	
	@Inject @Qualifier("siteModel")
	private Model siteModel;
		
	static def saveSite(Model model, def filename) throws ModelException {
		File outFile = new File(filename);
		try {
			FileOutputStream fos = new FileOutputStream(outFile);
			model.write(fos, "TURTLE");
		} catch (FileNotFoundException e) {
			throw new ModelException("Could not save file due to " + e.getMessage());
		}
	}
	
	String writeSite(Model model, String format) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, format);
		return baos.toString("UTF-8");
	}
	
	def makeUrlPublic(String url)
	{
		return url.replaceAll(propStore.getPrivateUrl(), propStore.getProperty("public_url"));
	}
	
	def getLocalUrl(String url)
	{
		url.replace(propStore.getPrivateUrl(), "/");
	}
	
	private String decodeShortResource(String resource) {
		String nsPrefix = resource.replaceFirst('_.*$', "");
		String type = resource.replaceFirst("^.*_", "");
		String getUrl = siteModel.getNsPrefixURI(nsPrefix);
		return getUrl + type;
	}
	
	private String encodeResource(String resource) {
		String root = resource.replaceFirst('[a-zA-Z0-9-_:#.]*$', "");
		String suffix = resource.replaceFirst("^.*/", "");
		String prefix = siteModel.getNsURIPrefix(root);
		return prefix + "_" + suffix;
	}
}
