package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PostcodeValidationServiceImpl implements PostcodeValidationService {

    @Value("${CHL_POSTCODE_LOOKUP_URL}")
    private String postcodeLookupURL;

    private final RestTemplate restTemplate;

    @Autowired
    public PostcodeValidationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validUKPostcode(String postcode) {
        String lookupUrl = postcodeLookupURL + "/" + postcode;
        ResponseEntity<Void> response;
        try {
            response = restTemplate.getForEntity(lookupUrl, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

}
