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

package net.opentechnology.triki.core.renderer;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class ResourceRendererTest {

	@Test
	public void testResourceRendererSuccess() throws MalformedURLException {
		String privateUrl = "http://localhost:8080/";
		RelativeUrlRenderer renderer = new RelativeUrlRenderer(privateUrl);
		String testUrl = privateUrl  + "resource/blog2";
		String rendered = renderer.toString(new URL(testUrl), null, null);
		assertEquals("/resource/blog2", rendered);
	}

}
