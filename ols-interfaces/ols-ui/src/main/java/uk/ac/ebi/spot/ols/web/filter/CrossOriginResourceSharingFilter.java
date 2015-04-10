package uk.ac.ebi.spot.ols.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;


/**
 * Filters all requests and adds Cross Origin Resource Sharing (CORS) to indicate that GET requests to the OLS API are
 * allowed across all domains.
 *
 * @author Julie McMurry adapted from Tony Burdett and https://spring.io/guides/gs/rest-service-cors/
 * @date 2015-04-10
 *
 */
@Component
public class CrossOriginResourceSharingFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        // is this a CORS request?
        if (httpRequest.getHeader("Origin") != null) {
            String origin = httpRequest.getHeader("Origin");
            String requestURI = httpRequest.getRequestURI();
            getLog().trace("Possible cross-origin request received from '" + origin + "' to URI: " +
                                   "'" + requestURI + "'.  Enabling CORS.");

            // add CORS "pre-flight" request headers
            httpResponse.addHeader("Access-Control-Allow-Origin", "*");
            httpResponse.addHeader("Access-Control-Allow-Headers", "accept,Content-Type");
            httpResponse.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT");
            httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
