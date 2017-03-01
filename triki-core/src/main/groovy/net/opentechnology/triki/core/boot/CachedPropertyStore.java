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

package net.opentechnology.triki.core.boot;

import javax.inject.Inject;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;

public class CachedPropertyStore {
	
	@Inject	@Qualifier("siteModel")
	private Model model;

	private String contentDir;
	private int port;
	private String mode;

	public String getContentDir() {
		return contentDir;
	}

	public void setContentDir(String contentDir) {
		this.contentDir = contentDir;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	

	public String getPrivateUrl() {
		return "http://localhost:" + port + "/";
	}
	
	public String getProperty(String propName)
	{
		return "unknown";
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
