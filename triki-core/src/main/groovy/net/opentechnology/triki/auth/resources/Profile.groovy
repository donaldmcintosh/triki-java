package net.opentechnology.triki.auth.resources

import javax.servlet.http.HttpSession


class Profile {

    private String name
    private String email
    private String website
    private String identityProvider
    private Map<String, String> moduleParams = new HashMap<>()

    private Profile() {}

    public static Profile getProfile(HttpSession session){
        if(session.getAttribute(AuthenticateResource.SESSION_PROFILE)){
            return session.getAttribute(AuthenticateResource.SESSION_PROFILE)
        }
        else {
            return new Profile();
        }
    }

    Map<String, String> getModuleParams() {
        return moduleParams
    }

    void setModuleParams(Map<String, String> moduleParams) {
        this.moduleParams = moduleParams
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
                ", identityProvider='" + identityProvider + '\'' +
                ", moduleParams=" + moduleParams +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
