package net.opentechnology.triki.auth.resources

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import groovy.util.logging.Log4j
import net.opentechnology.triki.auth.AuthenticationManager
import net.opentechnology.triki.auth.module.AuthModule
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.core.dto.SettingDto
import org.apache.http.impl.client.CloseableHttpClient
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.ws.rs.core.Context

@Log4j
class AuthenticateResourceTest extends Specification {

    CloseableHttpClient mockHttpClient = Mock(CloseableHttpClient)
    CachedPropertyStore mockCachedPropertyStore = Mock(CachedPropertyStore)
    AuthenticationManager mockAuthenticationManager = Mock(AuthenticationManager)
    Utilities mockUtilities = Mock(Utilities)
    SettingDto mockSettingDto = Mock(SettingDto)

    AuthenticateResource authenticateResource


    def setup() {
        authenticateResource = new AuthenticateResource(mockCachedPropertyStore, mockAuthenticationManager, mockUtilities, mockSettingDto)
    }

    def "OpenID Auth Login request"() {

        given: 'An open ID user'

        HttpServletResponse resp = Mock(HttpServletResponse)
        HttpServletRequest req = Mock(HttpServletRequest)
        HttpSession session = Mock(HttpSession)

        when: 'A call to login using OpenID'

        authenticateResource.getStateLogin(resp, req, '/blog/frenchdivide')

        then: 'Should authenticate'

        1 * req.getSession() >> session
        1 * session.getAttribute(AuthModule.SessionVars.OPENID_STATE.toString()) >> 'thebigsecret'
        1 * mockSettingDto.getSetting(AuthModule.Settings.GOOGLEAUTHENDPOINT.toString()) >> 'http://www.google.com/auth2'
        1 * mockSettingDto.getSetting(AuthModule.Settings.GOOGLECLIENTID.toString()) >> 'myClientId1'
        1 * mockSettingDto.getSetting(AuthModule.Settings.OPENIDCONNECTREDIRECTURI.toString()) >> 'http://www.donaldmcintosh.net/auth/openid'
        1 * mockSettingDto.getSetting(AuthModule.Settings.OPENIDSCOPE.toString()) >> 'openid'

        1 * resp.sendRedirect(_) >> { args  ->
            assert args[0] == 'http://www.google.com/auth2?client_id=myClientId1&redirect_uri=http%3A%2F%2Fwww.donaldmcintosh.net%2Fauth%2Fopenid&scope=openid&response_type=code&state=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXRoIjoiL2Jsb2cvZnJlbmNoZGl2aWRlIn0.s3BlYSqTw5dlkPcY3jhvjWpy7YHetWbJjOjUmpuo6_g'
        }

    }

    def "decode token"() {
        given: 'a token'

        String token = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImRvbmFsZGJtY2ludG9zaEB5YWhvby5jby51ayIsIm5hbWUiOiJkb25hbGRibWNpbnRvc2hAeWFob28uY28udWsiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1kMzh4UUdwOEozdy9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS96bDBDVVFkdTYzMC9waG90by5qcGciLCJsb2NhbGUiOiJlbiIsIm5pY2tuYW1lIjoiZG9uYWxkYm1jaW50b3NoIiwiYXBwX21ldGFkYXRhIjp7ImF1dGhvcml6YXRpb24iOnsiZ3JvdXBzIjpbXX19LCJhdXRob3JpemF0aW9uIjp7Imdyb3VwcyI6W119LCJncm91cHMiOltdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiY2xpZW50SUQiOiJrYnl1RkRpZExMbTI4MExJd1ZGaWF6T3FqTzN0eThLSCIsInVwZGF0ZWRfYXQiOiIyMDE4LTEwLTIxVDE1OjUxOjExLjc1OVoiLCJ1c2VyX2lkIjoiZ29vZ2xlLW9hdXRoMnwxMDcxNDYzMzY3NTA5MDU1Nzc1MzMiLCJpZGVudGl0aWVzIjpbeyJwcm92aWRlciI6Imdvb2dsZS1vYXV0aDIiLCJ1c2VyX2lkIjoiMTA3MTQ2MzM2NzUwOTA1NTc3NTMzIiwiY29ubmVjdGlvbiI6Imdvb2dsZS1vYXV0aDIiLCJpc1NvY2lhbCI6dHJ1ZX1dLCJjcmVhdGVkX2F0IjoiMjAxOC0xMC0xN1QxMjo1Mjo1Ni4wNTBaIiwiaXNzIjoiaHR0cHM6Ly9zYW1wbGVzLmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEwNzE0NjMzNjc1MDkwNTU3NzUzMyIsImF1ZCI6ImtieXVGRGlkTExtMjgwTEl3VkZpYXpPcWpPM3R5OEtIIiwiaWF0IjoxNTQwMTM3MDc3LCJleHAiOjE1NDAxNzMwNzd9.pRH_ImZ3J6e79LLHcMr19_A2ybXpiEmbXqOArzLwmPk'

        when: ' it is decoded'

        DecodedJWT jwt = JWT.decode(token)
        def claims = jwt.getClaims()

        then:

        claims.get('email').asString() == 'donaldbmcintosh@yahoo.co.uk'
        claims.get('name').asString() == 'donaldbmcintosh@yahoo.co.uk'
    }


    def "encode token"() {
        given: 'a token'

        String randomSecret = UUID.randomUUID().toString()
        Algorithm algorithm = Algorithm.HMAC256(randomSecret);
        String secretState = JWT.create().withClaim('path', '/').sign(algorithm)
        log.info(secretState)

        when: ' it is decoded'

        DecodedJWT jwt = JWT.decode(secretState)
        def claims = jwt.getClaims()

        then:

        claims.get('path').asString() == '/'
    }

}
