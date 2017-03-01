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

package net.opentechnology.triki.news.init

import org.apache.commons.configuration.Configuration;

import groovy.util.logging.Log4j
import net.opentechnology.triki.core.boot.CachedPropertyStore;

import javax.inject.Inject

import org.springframework.beans.factory.annotation.Value;

@Log4j
class LoadTrikiExtensions {
	
	@Inject
	private CachedPropertyStore props;
	
	private String extDir;

	public void init()
	{
		extDir = props.getContentDir() + "/groovy"
		GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader());
		def topDir = new File(extDir)
		if(topDir.exists())
		{
			topDir.eachFileMatch(~/.*.groovy/) { groovyFile ->
				Class extension = loader.parseClass(groovyFile)
				extension.newInstance()
				log.info("Loaded extension ${groovyFile}")
			}
		} 
	}
}
