package uk.gov.companieshouse.officerfiling.api.validation;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.validation.error.AddressErrorProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A singular validator to perform validation rules on residential (home) and correspondence (service) addresses.
 * The validator methods are unaware of which type of address is being validated (ie premises validation has the same rules regardless of the type of address).
 */
public class AddressValidator extends OfficerValidator {

    private static final Integer LENGTH_20 = 20;
    private static final Integer LENGTH_50 = 50;
    private static final Integer LENGTH_200 = 200;
    private final List<String> countryList;
    private final List<String> ukCountryList;

    public AddressValidator(Logger logger, CompanyProfileService companyProfileService, String inputAllowedNationalities, ApiEnumerations apiEnumerations, List<String> countryList, List<String> ukCountryList) {
        super(logger, companyProfileService, inputAllowedNationalities, apiEnumerations);
        this.countryList = countryList;
        this.ukCountryList = ukCountryList;
    }

    /**
     * Validate the given address, append any validation errors to the given errorList.
     *
     * @param addressErrorProvider Specifies which error messages will be used if any validation errors occur
     * @param request              Used to attach request information to any errors raised
     * @param errorList            All validation errors triggered will be appended to this list
     * @param address              The address being validated
     */
    public void validate(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, AddressDto address) {
        if (address == null) {
            createBlankValidationErrors(addressErrorProvider, request, errorList);
            return;
        }
        validatePremises(addressErrorProvider, request, errorList, address.getPremises());
        validateAddressLineOne(addressErrorProvider, request, errorList, address.getAddressLine1());
        validateOptionalAddressLineTwo(addressErrorProvider, request, errorList, address.getAddressLine2());
        validateLocality(addressErrorProvider, request, errorList, address.getLocality());
        validateOptionalRegion(addressErrorProvider, request, errorList, address.getRegion());
        validateCountry(addressErrorProvider, request, errorList, address.getCountry());
        validatePostalCode(addressErrorProvider, request, errorList, address.getPostalCode(), address.getCountry());
    }

    private void createBlankValidationErrors(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList) {
        createValidationError(request, errorList, addressErrorProvider.getPremisesBlank());
        createValidationError(request, errorList, addressErrorProvider.getAddressLineOneBlank());
        createValidationError(request, errorList, addressErrorProvider.getLocalityBlank());
        createValidationError(request, errorList, addressErrorProvider.getPostalCodeBlank());
        createValidationError(request, errorList, addressErrorProvider.getCountryBlank());
    }

    private void validatePremises(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String premises) {
        if (StringUtils.isBlank(premises)) {
            createValidationError(request, errorList, addressErrorProvider.getPremisesBlank());
        } else {
            if (!validateDtoFieldLength(premises, LENGTH_200)) {
                createValidationError(request, errorList, addressErrorProvider.getPremisesLength());
            }
            if (!isValidCharacters(premises)) {
                createValidationError(request, errorList, addressErrorProvider.getPremisesCharacters());
            }
        }
    }

    private void validateAddressLineOne(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String addressLineOne) {
        if (StringUtils.isBlank(addressLineOne)) {
            createValidationError(request, errorList, addressErrorProvider.getAddressLineOneBlank());
        } else {
            if (!validateDtoFieldLength(addressLineOne, LENGTH_50)) {
                createValidationError(request, errorList, addressErrorProvider.getAddressLineOneLength());
            }
            if (!isValidCharacters(addressLineOne)) {
                createValidationError(request, errorList, addressErrorProvider.getAddressLineOneCharacters());
            }
        }
    }

    private void validateOptionalAddressLineTwo(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String addressLineTwo) {
        if (!StringUtils.isBlank(addressLineTwo)) {
            if (!validateDtoFieldLength(addressLineTwo, LENGTH_50)) {
                createValidationError(request, errorList, addressErrorProvider.getAddressLineTwoLength());
            }
            if (!isValidCharacters(addressLineTwo)) {
                createValidationError(request, errorList, addressErrorProvider.getAddressLineTwoCharacters());
            }
        }
    }

    private void validateLocality(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String locality) {
        if (StringUtils.isBlank(locality)) {
            createValidationError(request, errorList, addressErrorProvider.getLocalityBlank());
        } else {
            if (!validateDtoFieldLength(locality, LENGTH_50)) {
                createValidationError(request, errorList, addressErrorProvider.getLocalityLength());
            }
            if (!isValidCharacters(locality)) {
                createValidationError(request, errorList, addressErrorProvider.getLocalityCharacters());
            }
        }
    }

    private void validateOptionalRegion(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String region) {
        if (!StringUtils.isBlank(region)) {
            if (!validateDtoFieldLength(region, LENGTH_50)) {
                createValidationError(request, errorList, addressErrorProvider.getRegionLength());
            }
            if (!isValidCharacters(region)) {
                createValidationError(request, errorList, addressErrorProvider.getRegionCharacters());
            }
        }
    }

    private void validateCountry(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String country) {
        if (StringUtils.isBlank(country)) {
            createValidationError(request, errorList, addressErrorProvider.getCountryBlank());
        } else {
            if (!countryList.stream().map(String::toLowerCase).toList().contains(country.toLowerCase())) {
                createValidationError(request, errorList, addressErrorProvider.getCountryInvalid());
            }
            if (!validateDtoFieldLength(country, LENGTH_50)) {
                createValidationError(request, errorList, addressErrorProvider.getCountryLength());
            }
            if (!isValidCharacters(country)) {
                createValidationError(request, errorList, addressErrorProvider.getCountryCharacters());
            }
        }
    }

    private void validatePostalCode(AddressErrorProvider addressErrorProvider, HttpServletRequest request, List<ApiError> errorList, String postalCode, String country) {
        if (!StringUtils.isBlank(postalCode)) {
            if (!isValidCharacters(postalCode)) {
                createValidationError(request, errorList, addressErrorProvider.getPostalCodeCharacters());
            }
            if (!validateDtoFieldLength(postalCode, LENGTH_20)) {
                createValidationError(request, errorList, addressErrorProvider.getPostalCodeLength());
            }
        }
        if ((StringUtils.isBlank(country) || isUkCountry(country)) && (postalCode == null || postalCode.isBlank())) {
            createValidationError(request, errorList, addressErrorProvider.getPostalCodeBlank());
        } else if (isUkCountry(country) && !isValidCharactersForUkPostcode(postalCode)) {
            createValidationError(request, errorList, addressErrorProvider.getPostalCodeUkInvalid());
        }
    }

    private boolean isUkCountry(String country) {
        return country != null && ukCountryList.stream().anyMatch(country::equalsIgnoreCase);
    }

}
