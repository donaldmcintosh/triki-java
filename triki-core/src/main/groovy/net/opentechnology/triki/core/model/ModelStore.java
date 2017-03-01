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

package net.opentechnology.triki.core.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.StartupException;

public class ModelStore {

	public static final String SITE_TTL = "site.ttl";
	private final Logger logger = Logger.getLogger(this.getClass());
	@Inject	@Qualifier("siteModel")
    private Model siteModel;
	
	@Inject
	private CachedPropertyStore propStore;
	
	public void initTripleStores() throws StartupException
	{
		String sitettl = propStore.getContentDir() + File.separator + "ttl" + File.separator + SITE_TTL;
		File siteGraphFile = new File(sitettl);
		try {
			if(siteGraphFile.exists())
			{
				logger.info("Found triple store ${SITE_TTL}, loading...");
				loadSite(sitettl, siteModel);
			}
		} catch(ModelException me)
		{
			throw new StartupException(me);
		}
	}

	public void loadSite(String ttlfile, Model model) throws ModelException {
		InputStream in = FileManager.get().open(ttlfile);
		if (in != null) {
			logger.info("Loading " + ttlfile);
			model.read(in, null, "TTL");
		}

	}
	
	public void saveModel(Model model, String filename) throws ModelException {
		File outFile = new File(propStore.getContentDir() + "/ttl", filename);
		
		try {
			synchronized(model)
			{
				FileOutputStream fos = new FileOutputStream(outFile);
				logger.info("Writing model to " + filename);
				model.write(fos, "TURTLE");
				fos.close();
			}
		} catch (Exception e) {
			throw new ModelException("Could not save file due to " + e.getMessage());
		}
	}

}
