package uk.ac.ebi.spot.ols.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 01/04/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class RedirectFilter {

    public static final String REWRITE_FILTER_CONF_PATH = "urlrewrite.xml";

    @Bean
     public FilterRegistrationBean filterRegistrationBean(final ServletContext servletContext)
     {
         FilterRegistrationBean registrationBean = new FilterRegistrationBean();

         registrationBean.setName("UrlRewriteFilter");
         registrationBean.setFilter(new UrlRewriteFilter());
         registrationBean.addUrlPatterns("*");
         registrationBean.addInitParameter("confPath", REWRITE_FILTER_CONF_PATH);

         registrationBean.addInitParameter("confReloadCheckInterval", "5");
         registrationBean.addInitParameter("logLevel", "INFO");

         return registrationBean;
     }

//    @Bean
//    public UrlRewriteFilter urlRewriteFilter(final ServletContext servletContext) throws ServletException {
//        UrlRewriteFilter urlRewriteFilter = new UrlRewriteFilter();
//        urlRewriteFilter.init(new FilterConfig() {
//            private final Map<String, String> params = new HashMap<String, String>();
//            {
//                params.put("confPath", "urlrewrite.xml");
//            }
//
//            @Override
//            public String getFilterName() {
//                return "UrlRewriteFilter";
//            }
//
//            @Override
//            public ServletContext getServletContext() {
//                return servletContext;
//            }
//
//            @Override
//            public String getInitParameter(String name) {
//                return params.get(name);
//            }
//
//            @Override
//            public Enumeration<String> getInitParameterNames() {
//                return Collections.enumeration(params.keySet());
//            }
//        });
//
//        return urlRewriteFilter;
//    }

}
