package uk.ac.ebi.spot.ols.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Jupp
 * @date 25/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {


    /**
     * This config stops the rest api from using suffix pattern matching on URLs to determine content type
     * e.g. .json for .xml in URL to get data back in specific formats
     *
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
            .setUseSuffixPatternMatch(false);
    }

}