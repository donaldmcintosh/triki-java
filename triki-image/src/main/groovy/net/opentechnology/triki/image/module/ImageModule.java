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

import javax.inject.Inject;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Qualifier;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.dto.GroupDto;
import net.opentechnology.triki.core.dto.PageDto;
import net.opentechnology.triki.core.dto.PrefixDto;
import net.opentechnology.triki.core.dto.PropertyDto;
import net.opentechnology.triki.core.dto.TypeDto;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.schema.Exif;
import net.opentechnology.triki.schema.Triki;

import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImageModule implements Module {
	
	@Inject	@Qualifier("siteModel")
	private Model model;	
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private PrefixDto prefixDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private PageDto pageDto;
	
	@Inject
	private GroupDto groupDto;
	
	@Inject
	private PropertyDto propertyDto;
	
	public void init()
	{
		coreModule.registerModules(this);
	}

	@Override
	public void initMod() {
		Resource publicGroup = groupDto.getGroup("public");
		prefixDto.addPrefix("image", props.getPrivateUrl() + "image/");
		prefixDto.addPrefix("exif", Exif.NS);
		
		propertyDto.addProperty("dateTimeOriginal", Exif.dateTimeOriginal.getURI(), 19);
		propertyDto.addProperty("originalWidth", Exif.width.getURI(), 19);
		propertyDto.addProperty("originalHeight", Exif.height.getURI(), 19);
		propertyDto.addProperty("thumbnail", Triki.thumbimg.getURI(), 19);
		
		typeDto.addTypeRestricted("image", "Image", publicGroup);
		typeDto.addTypeRestricted("images", "Images", publicGroup);
		
		pageDto.addIndexPage("images", typeDto.getType("images"), "Images Index", "public", typeDto.getType("image"));
	}
	

	@Override
	public void initWeb() {
		// Nothing to do
	}

}
