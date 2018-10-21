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

package net.opentechnology.triki.auth.resources

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import groovy.json.JsonSlurper
import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.core.dto.SettingDto
import groovy.util.logging.Log4j
import org.apache.http.client.utils.URIBuilder

import javax.inject.Inject
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam
import javax.ws.rs.GET
import javax.ws.rs.POST;
import javax.ws.rs.Path
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.Consts
import org.apache.jena.rdf.model.Resource
import org.apache.log4j.Logger
import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.AuthenticationManager
import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.core.resources.RenderResource;
import net.opentechnology.triki.schema.Dcterms
import net.opentechnology.triki.schema.Triki

@Log4j
@Path("/auth")
public class AuthenticateResource extends RenderResource {

    private final CloseableHttpClient httpclient = HttpClients.createDefault();
	public static final String SESSION_PERSON = "person";
	public static final String SESSION_ID = "id";
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Inject
	private CachedPropertyStore propStore;
	
	@Inject
	private final AuthenticationManager authMgr;
	
	@Inject
	private final Utilities utils;

	@Inject
	private final SettingDto settingDto;

	public AuthenticateResource(){

	}

	public AuthenticateResource(CachedPropertyStore propStore, AuthenticationManager authMgr, Utilities utils, SettingDto settingDto) {
		this.propStore = propStore
		this.authMgr = authMgr
		this.utils = utils
		this.settingDto = settingDto
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void login(@Context HttpServletResponse resp, @Context HttpServletRequest req,
			MultivaluedMap<String, String> formParams,
			@FormParam("action") String action,
			@FormParam("triki:login") String login,
			@FormParam("triki:password") String password) throws ServletException, IOException {
		HttpSession session = req.getSession();
		try {
			checkKnownAndForward(session, resp){ ->
				authMgr.authenticate(login, password);
			}
		} catch (AuthenticationException e) {
			logger.warn(login + "/" + password + " failed to authenticate");
			resp.sendRedirect("/login");
		}
	}
	
	@GET
	@Path("/logoff")	
	public String logoff(@Context HttpServletResponse resp, @Context HttpServletRequest req, @QueryParam("action") String action)
	{
		HttpSession session = req.getSession();
		session.removeAttribute(SESSION_PERSON);
		session.removeAttribute(SESSION_ID);
		
		String url =  propStore.getPrivateUrl();
		String content = renderContent(url);
		
		return content;
	}

	private setKnownPersonSession(HttpSession session, Resource person) {
		session.setAttribute(SESSION_PERSON, person)
	}
	
	private setAuthenticatedPersonLogin(HttpSession session, String id) {
		session.setAttribute(SESSION_ID, id)
	}
	
	@Path("indie")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response content(@Context HttpServletResponse resp,@Context HttpServletRequest req,
			@QueryParam("code") String code,
			@QueryParam("me") String me)
		throws ServletException, IOException, URISyntaxException
	{	
		HttpSession session = req.getSession();
		Calendar timestampCal = Calendar.getInstance();
		timestampCal.setTime(new Date());
		
		logger.info("${me} has tried to login with code ${code}...");
		
		HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.INDIELOGINROOT.toString()));
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("code", code));
		form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.INDIELOGINCLIENTID.toString())));
		form.add(new BasicNameValuePair("redirect_uri", settingDto.getSetting(AuthModule.Settings.INDIELOGINREDIRECTURI.toString())));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
		poster.setEntity(entity);

		CloseableHttpResponse response = httpclient.execute(poster);
		
		if(response.getStatusLine().getStatusCode() == Response.Status.OK.code)
		{
			logger.info("${me} successfully authenticated by indieauth");
			try {
				checkKnownAndForward(session, resp){ ->
					authMgr.authenticateByWebsite(me)
				}
			} catch (AuthenticationException e) {
				logger.info("Unknown person ${me} but is authenticated")
				setAuthenticatedPersonLogin(session, me);
				resp.sendRedirect("/");
			}
		}
		else
		{
			logger.warn("${me} not authenticated.");
			resp.sendRedirect("/resource/login");
		}
	}

	//https://accounts.google.com/o/oauth2/v2/auth?
	// client_id=173048672515-3bamo1l0dpoeo0u90q1ev0k4h9icjq7n.apps.googleusercontent.com&
	// redirect_uri=https://www.donaldmcintosh.net/auth/openidconnect&scope=openid+email&
	// response_type=code&
	// state=121ff877352548393447cb2fd58e69db513775d3

	@Path("openidlogin")
	@GET
	public Response getStateLogin(@Context HttpServletResponse resp,@Context HttpServletRequest req, @QueryParam("path") String path){
		HttpSession session = req.getSession();
		String randomSecret = UUID.randomUUID().toString()
		Algorithm algorithm = Algorithm.HMAC256(randomSecret);
		logger.info("Signing state token with secret ${randomSecret}")
		String secretState = JWT.create().withClaim('path', path ?: '/').sign(algorithm)
		session.setAttribute(AuthModule.SessionVars.OPENID_STATE.toString(), secretState)

		def authUrl = new URIBuilder(settingDto.getSetting(AuthModule.Settings.GOOGLEAUTHENDPOINT.toString()))
		authUrl.addParameter("client_id", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString()))
		authUrl.addParameter("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()))
		authUrl.addParameter("scope", settingDto.getSetting(AuthModule.Settings.OPENIDSCOPE.toString()))
		authUrl.addParameter("response_type", "code")
		authUrl.addParameter("state", secretState)

		logger.info("Requesting Google login with ${authUrl.build().toString()}")
		resp.sendRedirect(authUrl.build().toString());
	}

	@Path("openidconnect")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response openidConnect(@Context HttpServletResponse resp,@Context HttpServletRequest req,
								  @QueryParam("state") String state,
								  @QueryParam("code") String code,
								  @QueryParam("scope") String scope)
			throws ServletException, IOException, URISyntaxException
	{
		HttpSession session = req.getSession();
		Calendar timestampCal = Calendar.getInstance();
		timestampCal.setTime(new Date());
		def redirectPath

		String openIdState = session.getAttribute(AuthModule.SessionVars.OPENID_STATE.toString()) as String
		if(openIdState != state){
			logger.error("${openIdState} is not equal to anti-forge code ${state}, rejecting.")
			resp.sendRedirect("/");
		}
		else {
			DecodedJWT jwt = JWT.decode(openIdState);
			redirectPath = jwt.getClaim('path').asString()
		}

		HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.GOOGLETOKENENDPOINT.toString()));
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("code", code));
		form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString())));
		form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTSECRET.toString())));
		form.add(new BasicNameValuePair("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString())));
		form.add(new BasicNameValuePair("grant_type", "authorization_code"));
		logger.info("Calling Google with params:")
		form.each { param ->
			logger.info("${param.name}: ${param.value}")
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
		poster.setEntity(entity);

		CloseableHttpResponse response = httpclient.execute(poster);

		if(response.getStatusLine().getStatusCode() == Response.Status.OK.code)
		{
			def tokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

			logger.info("Successfully authenticated by OpenID Connect with token ${tokenResponse['id_token'] as String}");
			String email
			try {
				DecodedJWT jwt = JWT.decode(tokenResponse['id_token'] as String);
				email = jwt.getClaims().get('email').asString()

				checkKnownAndForward(session, resp){ ->
					authMgr.authenticateByEmail(email)
				}
			} catch (AuthenticationException e) {
				logger.info("Unknown person ${email} but is authenticated")
				resp.sendRedirect("/");
			} catch (JWTDecodeException jwte){
				logger.info("Problem decoding token")
				resp.sendRedirect("/");
			}
		}
		else
		{
			logger.warn(response.getEntity().getContent().toString());
			logger.warn("Google not not authenticated.");
			resp.sendRedirect("/resource/login");
		}
	}

	private checkKnownAndForward(HttpSession session, HttpServletResponse resp, Closure authMethod) {
		Resource person = authMethod();
		String id = person.getProperty(Dcterms.title).getString();
		setKnownPersonSession(session, person);
		Resource home = person.getPropertyResourceValue(Triki.home);
		if(session.getAttribute("origUrl") != null){
			logger.info("Redirecting ${id} to original URL");
			resp.sendRedirect(session.getAttribute("origUrl"));
		}
		else if(home != null)
		{
			String homeUrl = home.getURI().toString()
			logger.info("Redirecting ${id} to home page ${homeUrl}")
			resp.sendRedirect(utils.makeUrlPublic(homeUrl));
		}
		else
		{
			logger.info("Redirecting ${id} to home page")
			resp.sendRedirect("/");
		}
		
	}
	
}
