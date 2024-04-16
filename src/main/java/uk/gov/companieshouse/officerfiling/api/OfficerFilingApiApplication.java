package uk.gov.companieshouse.officerfiling.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OfficerFilingApiApplication {

    public static final String APPLICATION_NAME_SPACE = "officer-filing-api";

    public static void main(final String[] args) {
        SpringApplication.run(OfficerFilingApiApplication.class, args);
    }

}
