package uk.ac.ebi.spot.ols;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class SearchApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
