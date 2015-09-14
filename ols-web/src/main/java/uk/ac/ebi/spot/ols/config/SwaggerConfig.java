package uk.ac.ebi.spot.ols.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;

/**
 * @author Simon Jupp
 * @date 01/09/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
@EnableSwagger2
@ComponentScan(basePackages = "uk.ac.ebi.spot.ols.controller.api")
public class SwaggerConfig {


    @Bean
    public Docket petApi() {
      return new Docket(DocumentationType.SWAGGER_2)
          .select()

            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.regex("/api.*"))
            .build()
          .pathMapping("/")
          .directModelSubstitute(LocalDate.class,
                  String.class)
          .genericModelSubstitutes(ResponseEntity.class)
          .enableUrlTemplating(false)
          .apiInfo(new ApiInfo(
                  "EMBL-EBI Ontology Lookup Service API",
                  "The Ontology Lookup Service (OLS) provides a web service interface to query multiple ontologies from a single location with a unified output format (JSON). OLS can index any ontology available in the Open Biomedical Ontology (OBO) or Web Ontology Language (OWL) format.",
                  getClass().getPackage().getImplementationVersion(),
                  "https://www.ebi.ac.uk/about/terms-of-use",
                  "ols-support@ebi.ac.uk",
                  "Apache Version 2.0",
                  " http://www.apache.org/licenses/LICENSE-2.0"
          ));
    }


}
