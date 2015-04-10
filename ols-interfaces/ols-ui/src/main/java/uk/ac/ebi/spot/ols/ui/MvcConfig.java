package uk.ac.ebi.spot.ols.ui;

/**
 * Created by emma on 24/11/14.
 *
 * Configuration class for configuring Spring MVC in the application.
 *
 */

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // core pages
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/home").setViewName("index");
        registry.addViewController("/search").setViewName("search");
        registry.addViewController("/downloads").setViewName("downloads");
        registry.addViewController("/search/traits").setViewName("traitlist");

        // dynamically generated docs pages
        registry.addViewController("/docs").setViewName("docs");
        registry.addViewController("/docs/about").setViewName("docs-template");
        registry.addViewController("/docs/downloads").setViewName("docs-template");
        registry.addViewController("/docs/faq").setViewName("docs-template");
        registry.addViewController("/docs/methods").setViewName("docs-template");
        registry.addViewController("/docs/ontology").setViewName("docs-template");
        registry.addViewController("/docs/abbreviations").setViewName("docs-template");
        registry.addViewController("/docs/fileheaders").setViewName("docs-template");
        registry.addViewController("/docs/related-resources").setViewName("docs-template");
        registry.addViewController("/docs/programmatic-access").setViewName("docs-template");
        registry.addViewController("/docs/known-issues").setViewName("docs-template");

    }
}