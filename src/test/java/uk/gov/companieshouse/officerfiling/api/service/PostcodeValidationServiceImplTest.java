package uk.gov.companieshouse.officerfiling.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostcodeValidationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PostcodeValidationServiceImpl postcodeValidationService;

    private final static String postcodeLookupServiceURL = "https://postcode-lookup-service.com/lookup";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postcodeValidationService, "postcodeLookupURL", postcodeLookupServiceURL);
    }

    @Test
    void testValidUKPostcode() {
        //given
        String postcode = "SW1A2AA";
        String lookupUrl = postcodeLookupServiceURL + "/" + postcode;

        //when
        when(restTemplate.getForEntity(lookupUrl, Void.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //then
        assertTrue(postcodeValidationService.validUKPostcode(postcode));
    }

    @Test
    void testInvalidUKPostcode() {
        //given
        String postcode = "INVALID";
        String lookupUrl = postcodeLookupServiceURL + "/" + postcode;

        //when
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(lookupUrl, Void.class)).thenReturn(response);

        //then
        assertFalse(postcodeValidationService.validUKPostcode(postcode));
    }

    @Test
    void testPostcodeLookupServiceError() {
        //given
        String postcode = "SW1A2AA";
        String lookupUrl = postcodeLookupServiceURL + "/" + postcode;

        //when
        when(restTemplate.getForEntity(lookupUrl, Void.class)).thenThrow(new RuntimeException());

        //then
        assertFalse(postcodeValidationService.validUKPostcode(postcode));
    }
}