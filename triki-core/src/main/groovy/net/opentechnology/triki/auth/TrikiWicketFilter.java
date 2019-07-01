package net.opentechnology.triki.auth;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import org.apache.wicket.protocol.http.WicketFilter;

@WebFilter(value = "/*", initParams = {
    @WebInitParam(name = "applicationClassName", value ="net.opentechnology.triki.auth.LoginApplication"),
    @WebInitParam(name="filterMappingUrlPattern", value="/*")
})
public class TrikiWicketFilter extends WicketFilter {

}
