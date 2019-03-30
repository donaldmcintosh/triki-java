package net.opentechnology.triki.auth.resources


class Profile {

    private String name
    private String email
    private String website
    private String identityProvider
    private Map<String, String> oauthCredentials

    Map<String, String> getOauthCredentials() {
        return oauthCredentials
    }

    void setOauthCredentials(Map<String, String> oauthCredentials) {
        this.oauthCredentials = oauthCredentials
    }
    private boolean isAdmin = false

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getEmail() {
        return email
    }

    void setEmail(String email) {
        this.email = email
    }

    String getWebsite() {
        return website
    }

    void setWebsite(String website) {
        this.website = website
    }

    boolean getIsAdmin() {
        return isAdmin
    }

    void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin
    }

    String getIdentityProvider() {
        return identityProvider
    }

    void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider
    }

    String getPreferredDisplay(){
        if(email)
            return email
        else if(name)
            return name
        else if(website)
            return website
        else
            return "No identifier"
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
