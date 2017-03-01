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

package net.opentechnology.triki.share.module;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Qualifier

import groovy.util.logging.Log4j;
import net.opentechnology.triki.schema.Triki

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.CoreModule;
import net.opentechnology.triki.core.boot.StartupException;
import net.opentechnology.triki.core.dto.PageDto;
import net.opentechnology.triki.core.dto.PrefixDto;
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.core.dto.TypeDto;
import net.opentechnology.triki.modules.Module;
import net.opentechnology.triki.share.async.SendEmail;
import net.opentechnology.triki.share.async.SendTwitter

@Log4j
public class ShareModule implements Module {
	
	public enum Settings {
		EMAILUSERNAME,
		EMAILPASSWORD,
		IMAPHOST,
		EMAILFROM,
		TWITTERCONSUMERKEY,
		TWITTERCONSUMERSECRET,
		TWITTERACCESSTOKEN,
		TWITTERTOKENSECRET
	}
	
	@Inject	@Qualifier("siteModel")
	private Model model;	
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private PrefixDto prefixDto;

	@Inject
	private SettingDto settingDto;
	
	@Inject
	private TypeDto typeDto;
	
	@Inject
	private PageDto pageDto;
	
	@Inject
	private ResourceDto resourceDto;
	
	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private SendEmail emailSender;
	
	@Inject
	private SendTwitter twitterSender;
	
	public void init()
	{
		coreModule.registerModules(this);
	}

	@Override
	public void initMod() {
		String privateUrl = props.getPrivateUrl();
		prefixDto.addPrefix("note", props.getPrivateUrl() + "note/");
		prefixDto.addPrefix("notes", props.getPrivateUrl() + "notes/");

		typeDto.addType("note", "Note");
		typeDto.addType("notepub", "Post note");
		typeDto.addType("emailpub", "Send Email");
		
		pageDto.addPage("emailpub", typeDto.getType("emailpub"), "Send email page", "admin");
		pageDto.addPage("notepub", typeDto.getType("notepub"), "Post note page", "admin");
		Resource emailPub = pageDto.addPage("emailpub", typeDto.getType("emailpub"), "Send email", "admin");
		
		String recipients =""" 
		PREFIX triki: <http://www.opentechnology.net/triki/0.1/>
		PREFIX dc:    <http://purl.org/dc/terms/> 
		PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
		PREFIX resource: <${privateUrl}group/>
		
		SELECT ?target
			WHERE {  
			?target dc:title ?title .
			?target foaf:mbox ?mbox .
			?target foaf:member resource:friends .
		}
		""";
		
		resourceDto.checkString(emailPub, Triki.sparql, recipients);
		
		settingDto.addSetting(Settings.EMAILUSERNAME.name(), "emailuser");
		settingDto.addSetting(Settings.EMAILPASSWORD.name(), "emailpass");
		settingDto.addSetting(Settings.IMAPHOST.name(), "imaphost");
		settingDto.addSetting(Settings.EMAILFROM.name(), "emailfrom");
		
		settingDto.addSetting(Settings.TWITTERCONSUMERKEY.name(), "twitterconsumerkey");
		settingDto.addSetting(Settings.TWITTERCONSUMERSECRET.name(), "twitterconsumersecret");
		settingDto.addSetting(Settings.TWITTERACCESSTOKEN.name(), "twitteraccesstoken");
		settingDto.addSetting(Settings.TWITTERTOKENSECRET.name(), "twittertokensecret");
		
		pageDto.addIndexPage("notes", typeDto.getType("notes"), "Notes Index", "public", typeDto.getType("note"));
		
		initAsync();
	}
	
	public void initAsync() throws StartupException {
		try {
			camelCtx.addRoutes(emailSender);
			camelCtx.addRoutes(twitterSender);
		} catch (Exception e) {
			throw new StartupException(e);
		}
	}
	

	@Override
	public void initWeb() {
		// Nothing to do
	}

}
