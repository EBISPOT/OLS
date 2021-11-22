package uk.ac.ebi.spot.ols.config;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fasterxml.classmate.TypeResolver;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket postsApi() {
		
		TypeResolver typeResolver = new TypeResolver();
		
		return new Docket(DocumentationType.SWAGGER_2).select()
		         .apis(RequestHandlerSelectors.basePackage("uk.ac.ebi.spot.ols.controller.api")).build()	         
		         .alternateTypeRules( 
		        		 springfox.documentation.schema.AlternateTypeRules.newRule(
		                     typeResolver.resolve(Collection.class, 
		                         typeResolver.resolve(Map.class, String.class, typeResolver.resolve(Collection.class, String.class))),
		                     typeResolver.resolve(Collection.class, WildcardType.class), Ordered.HIGHEST_PRECEDENCE))      
		         .apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("TIB Terminology Service Documentation")
				.description("TIB Terminology Service API Reference for Developers")
				.termsOfServiceUrl("https://www.tib.eu/en/service/terms-of-use")
				.contact("TIB Terminology Service Development Team").license("imprint")
				.licenseUrl("https://service.tib.eu/ts4tib/imprint").version("1.0").build();
	}

}