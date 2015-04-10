package uk.ac.ebi.spot.ols.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Map;

/**
 * Filters requests that can take a JSONP callback parameter, and if present wraps the response in a javascript callback
 * function.
 * we should check whether response is already jsonp or not
 *
 * @author Julie McMurry adapted from Tony Burdett
 * @date 2015-04-10
 */
public class JsonpCallbackFilter implements Filter {
    private static final String callbackHook = "callback";
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void init(FilterConfig fConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        getLog().trace("Request received to URI: '" + ((HttpServletRequest) request).getRequestURI() + "'");

        @SuppressWarnings("unchecked")
        Map<String, String[]> params = (Map<String, String[]>) httpRequest.getParameterMap();
        // if we have a callback param, wrap function with jsonp, otherwise do nothing

        // if content type is already text/javascript, we need to avoid double-wrapping
        if (params.containsKey("callback") && !response.getContentType().equals("text/javascript;charset=UTF-8")) {
            getLog().trace("Wrapping response with JSONP callback '" + (params.get(callbackHook))[0] + "();'");
            String callback = params.get(callbackHook)[0];

            // create a request wrapper and remove the callback param
            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest);
            requestWrapper.removeAttribute(callbackHook);

            // create a response wrapper and use it to wrap response in a jsonp callback
            HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse);
            ServletOutputStream out = responseWrapper.getOutputStream();
            out.write((callback + "(").getBytes());
            chain.doFilter(requestWrapper, responseWrapper);
            out.write(");".getBytes());
            out.close();

            // update the content type of the response to javascript
            responseWrapper.setContentType("text/javascript;charset=UTF-8");
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
    }
}
