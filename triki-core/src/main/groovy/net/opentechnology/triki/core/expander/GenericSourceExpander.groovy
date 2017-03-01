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

package net.opentechnology.triki.core.expander;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import net.opentechnology.triki.core.resources.ContentUtils;
import net.opentechnology.triki.core.resources.ResourceException;

public class GenericSourceExpander extends AbstractSourceExpander implements SourceExpander {
	
	@Inject
	private ContentUtils contentUtils;
	
	private PegDownProcessor pdp = new PegDownProcessor(Extensions.TABLES);
	
	public String expand(String url) throws ExpanderException{
		
		if(url.endsWith("md"))
		{
			try {
				URL fullUrl = new URL(url);
				String filename = fullUrl.getPath().replaceAll("^.*/", "");
				String markdown = contentUtils.getClasspathTextContent(filename);
				return expandString(markdown);
			}  catch (MalformedURLException e) {
				throw new ExpanderException("Bad url " + url);
			} catch (ResourceException e) {
				throw new ExpanderException("Could not get url " + url);
			}
		}
		else
		{
			throw new ExpanderException("Unexpected file type for URL " + url);
		}
	}

	@Override
	public String expandString(String markdown) throws ExpanderException {
		String html = pdp.markdownToHtml(markdown);
		return html;
	}

}
