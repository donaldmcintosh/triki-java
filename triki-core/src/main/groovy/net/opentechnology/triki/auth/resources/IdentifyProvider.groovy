package net.opentechnology.triki.auth.resources

import org.apache.http.client.methods.CloseableHttpResponse

public interface IdentityProvider {
    String getName();
    void getMinimalProfile(Profile profile, CloseableHttpResponse response)
}
