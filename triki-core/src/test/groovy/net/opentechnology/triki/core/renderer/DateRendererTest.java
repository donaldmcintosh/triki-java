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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import org.apache.jena.datatypes.xsd.XSDDateTime;

public class DateRendererTest
{

    @Test
    public void testRendererSuccess()
    {
        DateRenderer renderer = new DateRenderer();
        XSDDateTime b = new XSDDateTime(Calendar.getInstance());
        System.out.println(renderer.toString(b, null, null));
    }
    
    @Test
    public void testRendererSuccessNoTime()
    {
        DateRenderer renderer = new DateRenderer();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(925827167000L));
        XSDDateTime date = new XSDDateTime(cal);
        assertEquals(renderer.toString(date, null, null), "Tue 04 May, 1999, 15:12");
    }

}
