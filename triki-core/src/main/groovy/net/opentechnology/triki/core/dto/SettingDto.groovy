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

package net.opentechnology.triki.core.dto;

import javax.inject.Inject;

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier;

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel;
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class SettingDto extends BaseDto {
	
	// No underscores allowed
	public enum Settings {
		RESTRICTION,
		CREATOR,
		YEARMONTHRESTRICTION,
		LIVEGRAPHSAVEPERIODMINS,
	}

	@Inject	@Qualifier("siteModel")
	private Model model;

	@Inject
	private GroupDto groupDto;
	
	@Inject
	private CachedPropertyStore props;

	
	public void addSetting(String setting, String value, String description)
	{
		String resName = props.getPrivateUrl() + "setting/" + setting;
		Resource contentResource = model.createResource(resName);
		checkResource(contentResource, RDF.type, Triki.Setting);
		checkResource(contentResource, Triki.restricted, groupDto.getGroup('private'));
		checkString(contentResource, DCTerms.title, setting);
		checkString(contentResource, Triki.setting, value);
		if(description){
			checkString(contentResource, DCTerms.description, description);
		}
	}

	public void addSetting(String setting, String value){
		addSetting(setting, value, null);
	}
	
	public String getSetting(String setting)
	{
		String settingValue = ""
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?value
		WHERE {  
			 ?sub triki:setting ?value .  
			 ?sub a triki:Setting .
			 ?sub dc:title "${setting}" .
        }
"""
	
		sparqler.execute(model, query){ QuerySolution soln ->
			Literal literal = soln.get("value").asLiteral();
			settingValue = literal.getString();
		}
		
		return settingValue;
	}
	
	public int getSettingAsInteger(String setting)
	{
		String value = getSetting(setting);
		return Integer.parseInt(value);
	}
	
	public Resource getSettingAsResource(String setting)
	{
		String value = getSetting(setting)
		String resName = props.getPrivateUrl() + value;
		Resource resource = model.createResource(resName);
		return resource;
	}

}
