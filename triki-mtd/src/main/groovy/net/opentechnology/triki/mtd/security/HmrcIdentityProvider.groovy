package net.opentechnology.triki.mtd.security


import com.auth0.jwt.exceptions.JWTDecodeException
import groovy.json.JsonSlurper
import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.resources.IdentityProvider
import net.opentechnology.triki.auth.resources.Profile
import net.opentechnology.triki.core.dto.GroupDto
import net.opentechnology.triki.core.dto.UserDto
import net.opentechnology.triki.schema.Foaf
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.log4j.Logger

import javax.inject.Inject

class HmrcIdentityProvider implements IdentityProvider {
    private final Logger logger = Logger.getLogger(this.getClass());

    public static String HMRC_TOKEN = "hmrctoken"

    @Inject
    private UserDto userDto

    @Inject
    private GroupDto groupDto

    @Override
    String getName() {
        return "hmrc"
    }

    @Override
    void getMinimalProfile(Profile profile, CloseableHttpResponse closeableHttpResponse) {
        Map<String, String> tokenResponse = new JsonSlurper().parse(closeableHttpResponse.getEntity().getContent());

        try {
            logger.info("Saving HMRC token")
            def hmrcGroup = groupDto.getGroup("hmrcvat")
            def privateGroup = groupDto.getGroup("private")
            def user
            if(profile.email) {
                user = userDto.getUserByEmail(profile.email)
            }
            if(!user) {
                logger.info("Adding user $profile.email")
                def userDetails = [:]
                userDetails."title" = profile.name
                userDetails."group" = privateGroup
                userDetails."member" = hmrcGroup
                userDetails."email" = profile.email
                String encodedName = profile.name ? profile.name?.replaceAll("[\\W]", "-") : profile.email?.replaceAll("[\\W]", "-");
                userDto.addUser(encodedName, userDetails)
            } else {
                logger.info("Adding user $profile.email to hmrcvat group")
                userDto.addResource(user, Foaf.member, hmrcGroup);
            }
            profile.getModuleParams().put(HMRC_TOKEN, tokenResponse['access_token'])

        } catch (JWTDecodeException jwte) {
            logger.info("Problem saving HMRC Token")
            throw new AuthenticationException(jwte)
        }
    }
}
