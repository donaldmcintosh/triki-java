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

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class BaseDto {

	public void checkString(Resource subject, Property prop, String literal) {
		if(literal != null && !subject.hasProperty(prop)){
			subject.addProperty(prop, literal);
		}
	}
	
	public void checkLiteral(Resource subject, Property prop, Literal literal) {
		if(literal != null && !subject.hasProperty(prop)){
			subject.addProperty(prop, literal);
		}
	}
	
	public void checkResource(Resource subject, Property prop, Resource res)
	{
		if(res != null && !subject.hasProperty(prop)){
			subject.addProperty(prop, res);
		}
	}
}
