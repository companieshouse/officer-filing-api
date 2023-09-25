package uk.gov.companieshouse.officerfiling.api.service;

public interface PostcodeValidationService {

    /**
     * Makes GET call with postcode to a postcode service.
     * @param postcode the postcode
     * @return 200 if postcode is valid, 404 if invalid
     */
    boolean validUKPostcode(final String postcode);
}
