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

package net.opentechnology.triki.mtd.module

import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.auth.resources.IdentityProviders
import net.opentechnology.triki.auth.resources.SessionUtils
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.boot.CoreModule
import net.opentechnology.triki.core.boot.StartupException
import net.opentechnology.triki.core.dto.*
import net.opentechnology.triki.core.template.TemplateStore
import net.opentechnology.triki.modules.Module
import net.opentechnology.triki.mtd.pages.*
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider
import net.opentechnology.triki.mtd.security.PageAuthStrategy
import net.opentechnology.triki.mtd.vatapi.serialisers.LocalDateConverter
import net.opentechnology.triki.schema.Triki
import org.apache.camel.CamelContext
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.wicket.ConverterLocator
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy
import org.apache.wicket.protocol.http.WebApplication
import org.apache.wicket.settings.SecuritySettings
import org.eclipse.jetty.servlet.ServletContextHandler
import org.springframework.beans.factory.annotation.Qualifier

import javax.inject.Inject
import java.time.LocalDate

public class HmrcVatModule implements Module {

	@Inject
	private ServletContextHandler sch;
	
	@Inject	@Qualifier("siteModel")
	private Model model;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private TypeDto typeDto;

	@Inject
	private PrefixDto prefixDto;

	@Inject
	private GroupDto groupDto;

	@Inject
	private CamelContext camelCtx;
	
	@Inject
	private CoreModule coreModule;
	
	@Inject
	private SettingDto settingDto;

	@Inject
	private ContentDto contentDto;

	@Inject
	private PageDto pageDto;
	
	@Inject
	private IdentityProviderDto identityProviderDto;

	@Inject
	private IdentityProviders identityProviders;

	@Inject
	private HmrcIdentityProvider hmrcIdentityProvider;

	@Inject
	private TemplateStore templateStore;

	private Resource vatUsersGroup;

	public void init()
	{
		coreModule.registerModules(this);
	}

	public enum Settings {
        HMRCBASEURL
	}

	@Override
	public void initMod() throws StartupException {
		initIdentityProviders();
		initGroups();
		initContent();
		initTypes();
		initPages();
		initAsync();
		initSettings();
	}

	private void initGroups(){
		Resource authRoot = model.createResource(props.getPrivateUrl() + "/auth/");
		authRoot.addProperty(Triki.unrestricted, ".*/mtdvatui/.*");
		vatUsersGroup = groupDto.addGroup("hmrcvat", "HMRC VAT users group");
	}

	private void initTypes() { ;

	}

	private void initPages() {
		pageDto.addPage("tandc", typeDto.getType("page"), "Terms and Conditions", "public");
		pageDto.addPage("privacy", typeDto.getType("page"), "Privacy Policy", "public");
	}

	private void initContent(){
		contentDto.addContent("vathome.md");
		contentDto.addContent("privacy.md");
		contentDto.addContent("tc.md");
		contentDto.addContent("mtd-summary.md");

		contentDto.addContent("antifraud.js");

		contentDto.addContent("hmrc.svg");
	}

	private void initIdentityProviders(){
		identityProviderDto.addIdentityProvider("hmrc", "https://api.service.hmrc.gov.uk/oauth/authorize", "https://api.service.hmrc.gov.uk/oauth/token", "read:vat write:vat");
		identityProviderDto.addIdentityProvider("hmrctest", "https://test-api.service.hmrc.gov.uk/oauth/authorize", "https://test-api.service.hmrc.gov.uk/oauth/token", "read:vat write:vat");
        identityProviderDto.addIdentityProvider("hmrcmock", "http://localhost:9090/oauth/authorize", "http://localhost:9090/oauth/token", "read:vat write:vat");
		identityProviderDto.addIdentityProvider("googlemock", "http://localhost:9090/o/oauth2/v2/auth", "http://localhost:9090/token", "openid email profile");

		identityProviders.getOauthProviders().put("hmrc", hmrcIdentityProvider);
		identityProviders.getOauthProviders().put("hmrctest", hmrcIdentityProvider);
        identityProviders.getOauthProviders().put("hmrcmock", hmrcIdentityProvider);
		identityProviders.getOauthProviders().put("googlemock", identityProviders.getIdentityProvider("google"));
	}

	private void initSettings(){
		settingDto.addSetting(Settings.HMRCBASEURL.name(), "https://test-api.service.hmrc.gov.uk/", "HMRC Base URL");
		settingDto.updateSetting(AuthModule.Settings.DEFAULTLOGINPAGE.name(), "/ui/mtdlogin", "MTD Default login page");
	}
	
	public void initAsync() throws StartupException {

	}

	@Override
	public void initWeb() {
	}

	@Override
	void addConverters(ConverterLocator defaultLocator) {
		defaultLocator.set(LocalDate.class, new LocalDateConverter());
	}

	@Override
	public void mountPages(WebApplication webApplication){
		webApplication.mountPage("/mtd/login", MtdLogin.class)
		webApplication.mountPage("/mtd/vat", MtdVatHome.class)
		webApplication.mountPage("/mtd/vat/obligations", MtdVatObligations.class)
		webApplication.mountPage("/mtd/vat/returns", MtdVatReturns.class)
		webApplication.mountPage("/mtd/vat/liabilities", MtdVatLiabilities.class)
		webApplication.mountPage("/mtd/vat/payments", MtdVatPayments.class)
	}

	@Override
	void setAuthorisationStrategy(SecuritySettings securitySettings) {
		PageAuthStrategy pageAuthStrategy = new PageAuthStrategy(MtdVatManage.class, MtdLogin.class);

		securitySettings.setAuthorizationStrategy(pageAuthStrategy);
	}
}
