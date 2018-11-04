package net.opentechnology.triki.auth.resources

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.core.dto.SettingDto
import org.apache.http.NameValuePair
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.log4j.Logger

import javax.inject.Inject

public interface IdentityProvider {
    String getName();
    URIBuilder getAuthUri();
    HttpPost getTokenPost(ArrayList<NameValuePair> form)
    Profile getMinimalProfile(CloseableHttpResponse response)
}

class GoogleIdentityProvider implements IdentityProvider {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    private final SettingDto settingDto;

    @Override
    String getName() {
        return 'google'
    }

    @Override
    URIBuilder getAuthUri() {
        def authUrl = new URIBuilder(settingDto.getSetting(AuthModule.Settings.GOOGLEAUTHENDPOINT.toString()))
        authUrl.addParameter("client_id", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString()))
        authUrl.addParameter("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()))
        authUrl.addParameter("response_type", "code")
        authUrl.addParameter("scope", settingDto.getSetting(AuthModule.Settings.GOOGLEOPENIDSCOPE.toString()))
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.GOOGLETOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTSECRET.toString())));
        poster
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
    URIBuilder getAuthUri() {
        def authUrl = new URIBuilder(settingDto.getSetting(AuthModule.Settings.YAHOOAUTHENDPOINT.toString()))
        authUrl.addParameter("client_id", settingDto.getSetting(AuthModule.Settings.YAHOOCLIENTID.toString()))
        authUrl.addParameter("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()))
        authUrl.addParameter("response_type", "code")
        authUrl.addParameter("scope", settingDto.getSetting(AuthModule.Settings.YAHOOOPENIDSCOPE.toString()))
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.YAHOOTOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.YAHOOCLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.YAHOOCLIENTSECRET.toString())));
        poster
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
    URIBuilder getAuthUri() {
        def authUrl = new URIBuilder(settingDto.getSetting(AuthModule.Settings.AMAZONAUTHENDPOINT.toString()))
        authUrl.addParameter("client_id", settingDto.getSetting(AuthModule.Settings.AMAZONCLIENTID.toString()))
        authUrl.addParameter("redirect_uri", settingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()))
        authUrl.addParameter("response_type", "code")
        authUrl.addParameter("scope", settingDto.getSetting(AuthModule.Settings.AMAZONOPENIDSCOPE.toString()))
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.AMAZONTOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.AMAZONCLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.AMAZONCLIENTSECRET.toString())));
        poster
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
        } catch (Exception e){
            throw new AuthenticationException("Problems getting Amazon profile: ${e.getMessage()}")
        }

        profile
    }
}

class IdentityProviders {

    @Inject
    AmazonIdentityProvider amazonIdentifyProvider

    @Inject
    YahooIdentityProvider yahooIdentifyProvider

    @Inject
    GoogleIdentityProvider googleIdentifyProvider
    
    IdentityProvider getIdentityProvider(String identifyProviderName){
        if(identifyProviderName == amazonIdentifyProvider.name){
            return amazonIdentifyProvider
        } else if(identifyProviderName == yahooIdentifyProvider.name){
            return yahooIdentifyProvider
        } else if(identifyProviderName == googleIdentifyProvider.name){
            return googleIdentifyProvider
        } else {
            throw new AuthenticationException("Unexpected identify provider " + identifyProviderName)
        }
        
    }
    
    
}
