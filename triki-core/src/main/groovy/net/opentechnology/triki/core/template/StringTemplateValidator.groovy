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

package net.opentechnology.triki.core.template

import java.util.List

import org.stringtemplate.v4.STErrorListener
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;
import org.stringtemplate.v4.misc.ErrorManager
import org.stringtemplate.v4.misc.STMessage;

import net.opentechnology.triki.modules.ContentValidator

class StringTemplateValidator implements ContentValidator {

	@Override
	public void validate(String text, List<String> errors) {
		STGroup g = new STGroupString("", "", '$' as char, '$' as char);
		g.setListener(new ErrorListener(errors));
		try {
			g.defineTemplate("validate", text)
		}
		catch(Exception e)
		{
			System.out.println("errors")
			errors << e.getMessage();
		}
	}

}

public class ErrorListener implements STErrorListener {
	
	private List<String> errors;
	
	public ErrorListener(List<String> errors)
	{
		this.errors = errors;
	}
	

	@Override
	public void compileTimeError(STMessage msg) {
		errors << msg.toString()
	}

	@Override
	public void runTimeError(STMessage msg) {
		errors << msg.toString()
	}

	@Override
	public void IOError(STMessage msg) {
		errors << msg.toString()
	}

	@Override
	public void internalError(STMessage msg) {
		errors << msg.toString()
	}
	
}
