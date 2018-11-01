package net.opentechnology.triki.auth.resources

import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.core.dto.SettingDto
import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.message.BasicNameValuePair

import javax.inject.Inject

public interface IdentityProvider {
    String getName();
    URIBuilder getAuthUri();
    HttpPost getTokenPost(ArrayList<NameValuePair> form)
}

class GoogleIdentityProvider implements IdentityProvider {

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
        authUrl.addParameter("scope", settingDto.getSetting(AuthModule.Settings.OPENIDSCOPE.toString()))
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.GOOGLETOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.GOOGLECLIENTSECRET.toString())));
        poster
    }
}

class YahooIdentityProvider implements IdentityProvider {

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
        authUrl.addParameter("response_type", "token")
        authUrl.addParameter("nonce", UUID.randomUUID().toString())
        authUrl.addParameter("scope", "openid")
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.YAHOOTOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.YAHOOCLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.YAHOOCLIENTSECRET.toString())));
        poster
    }
}

class AmazonIdentityProvider implements IdentityProvider {

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
        authUrl.addParameter("scope", "profile")
        authUrl
    }

    @Override
    HttpPost getTokenPost(ArrayList<NameValuePair> form) {
        HttpPost poster = new HttpPost(settingDto.getSetting(AuthModule.Settings.AMAZONTOKENENDPOINT.toString()));
        form.add(new BasicNameValuePair("client_id", settingDto.getSetting(AuthModule.Settings.AMAZONCLIENTID.toString())));
        form.add(new BasicNameValuePair("client_secret", settingDto.getSetting(AuthModule.Settings.AMAZONCLIENTSECRET.toString())));
        poster
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
