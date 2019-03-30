package net.opentechnology.triki.auth.resources

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.core.dto.IdentityProviderDto
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.schema.Triki
import org.apache.http.NameValuePair
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.jena.rdf.model.Resource
import org.apache.log4j.Logger

import javax.inject.Inject

public interface IdentityProvider {
    String getName();
    Profile getMinimalProfile(CloseableHttpResponse response)
}

class GoogleIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    String getName() {
        return 'google'
    }

    @Override
    Profile getMinimalProfile(CloseableHttpResponse response) {
        def profile = new Profile()
        def tokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

        try {
            DecodedJWT jwt = JWT.decode(tokenResponse['id_token'] as String);
            // Remove later
            jwt.getClaims().each { claim ->
                logger.info("Claim is ${claim.key} : ${claim.getValue().asString()}")
            }
            profile.setEmail(jwt.getClaims().get('email').asString())
            profile.setName(jwt.getClaims().get('name').asString())
            profile.setIdentityProvider(getName())
        } catch (JWTDecodeException jwte){
            logger.info("Problem decoding token for token from Google")
            throw new AuthenticationException(jwte)
        }

        profile
    }
}

class YahooIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    private final SettingDto settingDto;

    @Override
    String getName() {
        return 'yahoo'
    }

    @Override
    Profile getMinimalProfile(CloseableHttpResponse response) {
        def profile = new Profile()
        def tokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

        try {
            DecodedJWT jwt = JWT.decode(tokenResponse['id_token'] as String);
            jwt.getClaims().each { claim ->
                logger.info("Claim is ${claim.key} : ${claim.getValue().asString()}")
            }
            profile.setName(jwt.getClaims().get('name')?.asString())

            def binding = ["guid":  tokenResponse['xoauth_yahoo_guid']]
            def engine = new SimpleTemplateEngine()
            def template = engine.createTemplate(settingDto.getSetting(AuthModule.Settings.YAHOOPROFILEENDPOINT.toString())).make(binding)
            CloseableHttpClient httpclient = HttpClients.createDefault();
            URIBuilder urlBuilder = new URIBuilder(template.toString())
            HttpGet httpGet = new HttpGet(urlBuilder.build());
            httpGet.setHeader("Authorization","Bearer ${tokenResponse['access_token']}");
            CloseableHttpResponse getResponse = httpclient.execute(httpGet)
            def profileToken = new JsonSlurper().parse(getResponse.getEntity().getContent());

            profile.setEmail(profileToken.profile.email as String)

        } catch (JWTDecodeException jwte){
            logger.info("Problem decoding token for token from Google")
            throw new AuthenticationException(jwte)
        }

        profile
    }
}

class AmazonIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    private final SettingDto settingDto;

    @Override
    String getName() {
        return 'amazon'
    }

    @Override
    Profile getMinimalProfile(CloseableHttpResponse response) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        def profile = new Profile()
        def accessTokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

        try {
            URIBuilder urlBuilder = new URIBuilder(settingDto.getSetting(AuthModule.Settings.AMAZONPROFILEENDPOINT.toString()))
            urlBuilder.addParameter("access_token", accessTokenResponse['access_token'] as String)
            HttpGet httpGet = new HttpGet(urlBuilder.build());
            CloseableHttpResponse getResponse = httpclient.execute(httpGet)
            def profileToken = new JsonSlurper().parse(getResponse.getEntity().getContent());

            profile.setName(profileToken['name'] as String)
            profile.setEmail(profileToken['email'] as String)
            profile.setIdentityProvider(getName())
        } catch (Exception e){
            throw new AuthenticationException("Problems getting Amazon profile: ${e.getMessage()}")
        }

        profile
    }
}

class OutlookIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    private final SettingDto settingDto;

    @Override
    String getName() {
        return 'outlook'
    }

    @Override
    Profile getMinimalProfile(CloseableHttpResponse response) {
        def profile = new Profile()
        Map<String, String> tokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

        try {
            DecodedJWT jwt = JWT.decode(tokenResponse['id_token'] as String);
            jwt.getClaims().each { claim ->
                logger.info("Claim is ${claim.key} : ${claim.getValue().asString()}")
            }

            profile.setName(jwt.getClaims().find{it.key == 'name'}.getValue().asString())
            profile.setEmail(jwt.getClaims().find{it.key == 'email'}.getValue().asString())
            profile.setIdentityProvider(getName())
        } catch (Exception e){
            throw new AuthenticationException("Problems getting Outlook profile: ${e.getMessage()}")
        }

        profile
    }
}

class GenericIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    String getName() {
        return 'generic'
    }

    @Override
    Profile getMinimalProfile(CloseableHttpResponse response) {
        def profile = new Profile()
        Map<String, String> tokenResponse = new JsonSlurper().parse(response.getEntity().getContent());

        try {
            DecodedJWT jwt = JWT.decode(tokenResponse['id_token'] as String);
            jwt.getClaims().each { claim ->
                logger.info("Claim is ${claim.key} : ${claim.getValue().asString()}")
            }

            profile.setOauthCredentials(tokenResponse)
            profile.getOauthCredentials().keySet().each { key ->
                logger.info("Credentials is ${key} : ${profile.getOauthCredentials().get(key)}")
            }
        } catch (JWTDecodeException jwte){
            logger.info("Problem decoding token for token")
            throw new AuthenticationException(jwte)
        }

        profile
    }
}

class IdentityProviders {

    private Map<String, IdentityProvider> oauthProviders = new HashMap<>();

    @Inject
    private IdentityProviders(AmazonIdentityProvider amazonIdentifyProvider, YahooIdentityProvider yahooIdentifyProvider,
                                    GoogleIdentityProvider googleIdentifyProvider, OutlookIdentityProvider outlookIdentityProvider){
        oauthProviders.put('google', googleIdentifyProvider);
        oauthProviders.put('yahoo', yahooIdentifyProvider);
        oauthProviders.put('amazon', amazonIdentifyProvider);
        oauthProviders.put('outlook', outlookIdentityProvider);

        oauthProviders.put('generic', new GenericIdentityProvider());
    }

    public Map<String, IdentityProvider> getOauthProviders() {
        return oauthProviders
    }

    @Inject
    private SettingDto settingDto

    @Inject
    private final IdentityProviderDto identityProviderDto;

    URIBuilder getAuthUri(String alias) {
        def authUrl
        identityProviderDto.getIdentityProvider(alias){ Resource idpResource ->
            authUrl  = new URIBuilder(idpResource.getProperty(Triki.oauthauthendpoint).getString())
            authUrl.addParameter("client_id", idpResource.getProperty(Triki.oauthclientid).getString())
            authUrl.addParameter("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()))
            authUrl.addParameter("response_type", "code")
            authUrl.addParameter("scope", idpResource.getProperty(Triki.oauthscope).getString())
        }

        authUrl
    }

    HttpPost getTokenPost(String alias, ArrayList<NameValuePair> form) {
        HttpPost poster
        identityProviderDto.getIdentityProvider(alias) { Resource idpResource ->
            poster = new HttpPost(idpResource.getProperty(Triki.oauthtokenendpoint).getString());
            form.add(new BasicNameValuePair("client_id",  idpResource.getProperty(Triki.oauthclientid).getString()));
            form.add(new BasicNameValuePair("client_secret",  idpResource.getProperty(Triki.oauthclientsecret).getString()));
        }
        poster
    }
    
    IdentityProvider getIdentityProvider(String identifyProviderName){
        if(oauthProviders.containsKey(identifyProviderName)){
            return oauthProviders.get(identifyProviderName)
        } else {
            return oauthProviders.get('generic')
        }
    }

}
