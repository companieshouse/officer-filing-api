package uk.gov.companieshouse.officerfiling.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.validation.error.AddressErrorProvider;
import uk.gov.companieshouse.officerfiling.api.validation.error.CorrespondenceAddressErrorProvider;
import uk.gov.companieshouse.officerfiling.api.validation.error.ResidentialAddressErrorProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressValidatorTest {

    private static final String ALLOWED_NATIONALITIES = "";
    private static final List<String> COUNTRY_LIST = List.of("England", "Wales", "Scotland", "Northern Ireland", "France", "VeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountry", "England§");
    private static final List<String> UK_COUNTRY_LIST = List.of("England", "Wales", "Scotland", "Northern Ireland");

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private Logger mockLogger;
    @Mock
    private AddressDto mockAddress;
    @Mock
    private CompanyProfileServiceImpl mockCompanyProfileService;
    @Mock
    private ApiEnumerations mockApiEnumerations;

    private AddressValidator addressValidator;
    private List<ApiError> apiErrorList;
    private AddressErrorProvider residentialAddressErrorProvider;
    private AddressErrorProvider correspondenceAddressErrorProvider;

    @BeforeEach
    void setUp() {
        addressValidator = new AddressValidator(mockLogger, mockCompanyProfileService, ALLOWED_NATIONALITIES, mockApiEnumerations, COUNTRY_LIST, UK_COUNTRY_LIST);
        apiErrorList = new ArrayList<>();
        residentialAddressErrorProvider = new ResidentialAddressErrorProvider(mockApiEnumerations);
        correspondenceAddressErrorProvider = new CorrespondenceAddressErrorProvider(mockApiEnumerations);
    }

    @ParameterizedTest
    @CsvSource({"The Lane,London", "'',''", ","})
    void validateWhenAddressFieldsAreValid(String addressLine2, String region) {
        when(mockAddress.getPremises()).thenReturn("11");
        when(mockAddress.getAddressLine1()).thenReturn("Street Road");
        when(mockAddress.getAddressLine2()).thenReturn(addressLine2);
        when(mockAddress.getLocality()).thenReturn("London");
        when(mockAddress.getRegion()).thenReturn(region);
        when(mockAddress.getCountry()).thenReturn("England");
        when(mockAddress.getPostalCode()).thenReturn("TE53 3ST");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("No errors should be produced when all fields are valid")
                .isEmpty();
    }

    @Test
    void validateWhenResidentialAddressIsNull() {
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK)).thenReturn("Premises Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK)).thenReturn("Address line 1 Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK)).thenReturn("Locality Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn("Country Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn("Postal code Blank");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("An error should be produced for each mandatory field")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Premises Blank")
                .contains("Address line 1 Blank")
                .contains("Locality Blank")
                .contains("Country Blank")
                .contains("Postal code Blank");
    }

    @Test
    void validateWhenCorrespondenceAddressIsNull() {
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK)).thenReturn("Premises Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK)).thenReturn("Address line 1 Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK)).thenReturn("Locality Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn("Country Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn("Postal code Blank");

        addressValidator.validate(correspondenceAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("An error should be produced for each mandatory field")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Premises Blank")
                .contains("Address line 1 Blank")
                .contains("Locality Blank")
                .contains("Country Blank")
                .contains("Postal code Blank");
    }

    @Test
    void validateWhenAddressFieldsAreBlank() {
        when(mockAddress.getPremises()).thenReturn("");
        when(mockAddress.getAddressLine1()).thenReturn(" ");
        when(mockAddress.getAddressLine2()).thenReturn("");
        when(mockAddress.getLocality()).thenReturn("");
        when(mockAddress.getRegion()).thenReturn(" ");
        when(mockAddress.getCountry()).thenReturn("");
        when(mockAddress.getPostalCode()).thenReturn("       ");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK)).thenReturn("Premises Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK)).thenReturn("Address line 1 Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK)).thenReturn("Locality Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn("Country Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn("Postal code Blank");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("An error should be produced for each mandatory field")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Premises Blank")
                .contains("Address line 1 Blank")
                .contains("Locality Blank")
                .contains("Country Blank")
                .contains("Postal code Blank");
    }

    @Test
    void validateWhenAddressFieldsAreNull() {
        when(mockAddress.getPremises()).thenReturn(null);
        when(mockAddress.getAddressLine1()).thenReturn(null);
        when(mockAddress.getAddressLine2()).thenReturn(null);
        when(mockAddress.getLocality()).thenReturn(null);
        when(mockAddress.getRegion()).thenReturn(null);
        when(mockAddress.getCountry()).thenReturn(null);
        when(mockAddress.getPostalCode()).thenReturn(null);
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK)).thenReturn("Premises Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK)).thenReturn("Address line 1 Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK)).thenReturn("Locality Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn("Country Blank");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn("Postal code Blank");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("An error should be produced for each mandatory field")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Premises Blank")
                .contains("Address line 1 Blank")
                .contains("Locality Blank")
                .contains("Country Blank")
                .contains("Postal code Blank");
    }

    @Test
    void validateWhenResidentialAddressFieldsAreTooLong() {
        when(mockAddress.getPremises()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getAddressLine1()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getAddressLine2()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getLocality()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getRegion()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getCountry()).thenReturn("VeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountry");
        when(mockAddress.getPostalCode()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_LENGTH)).thenReturn("Premises Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_LENGTH)).thenReturn("Address line 1 Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_LENGTH)).thenReturn("Address line 2 Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_LENGTH)).thenReturn("Region Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_LENGTH)).thenReturn("Locality Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_LENGTH)).thenReturn("Country Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_LENGTH)).thenReturn("Postal code Length");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("A length error should be produced for each field")
                .hasSize(7)
                .extracting(ApiError::getError)
                .contains("Premises Length")
                .contains("Address line 1 Length")
                .contains("Address line 2 Length")
                .contains("Region Length")
                .contains("Locality Length")
                .contains("Country Length")
                .contains("Postal code Length");
    }

    @Test
    void validateWhenCorrespondenceAddressFieldsAreTooLong() {
        when(mockAddress.getPremises()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getAddressLine1()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getAddressLine2()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getLocality()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getRegion()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockAddress.getCountry()).thenReturn("VeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountryVeryLongCountry");
        when(mockAddress.getPostalCode()).thenReturn("abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_LENGTH)).thenReturn("Premises Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_LENGTH)).thenReturn("Address line 1 Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_TWO_LENGTH)).thenReturn("Address line 2 Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_REGION_LENGTH)).thenReturn("Region Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_LENGTH)).thenReturn("Locality Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_LENGTH)).thenReturn("Country Length");
        when(mockApiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_LENGTH)).thenReturn("Postal code Length");

        addressValidator.validate(correspondenceAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("A length error should be produced for each field")
                .hasSize(7)
                .extracting(ApiError::getError)
                .contains("Premises Length")
                .contains("Address line 1 Length")
                .contains("Address line 2 Length")
                .contains("Region Length")
                .contains("Locality Length")
                .contains("Country Length")
                .contains("Postal code Length");
    }

    @Test
    void validateWhenAddressFieldsHaveInvalidCharacter() {
        when(mockAddress.getPremises()).thenReturn("§");
        when(mockAddress.getAddressLine1()).thenReturn("§");
        when(mockAddress.getAddressLine2()).thenReturn("§");
        when(mockAddress.getLocality()).thenReturn("§");
        when(mockAddress.getRegion()).thenReturn("§");
        when(mockAddress.getCountry()).thenReturn("England§");
        when(mockAddress.getPostalCode()).thenReturn("§");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_CHARACTERS)).thenReturn("Premises Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_CHARACTERS)).thenReturn("Address line 1 Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_CHARACTERS)).thenReturn("Address line 2 Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_CHARACTERS)).thenReturn("Region Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_CHARACTERS)).thenReturn("Locality Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_CHARACTERS)).thenReturn("Country Characters");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_CHARACTERS)).thenReturn("Postal code Characters");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("A character error should be produced for each field")
                .hasSize(7)
                .extracting(ApiError::getError)
                .contains("Premises Characters")
                .contains("Address line 1 Characters")
                .contains("Address line 2 Characters")
                .contains("Region Characters")
                .contains("Locality Characters")
                .contains("Country Characters")
                .contains("Postal code Characters");
    }

    @Test
    void validateWhenCountryIsInvalid() {
        when(mockAddress.getPremises()).thenReturn("11");
        when(mockAddress.getAddressLine1()).thenReturn("Street Road");
        when(mockAddress.getAddressLine2()).thenReturn("The Lane");
        when(mockAddress.getLocality()).thenReturn("London");
        when(mockAddress.getRegion()).thenReturn("London");
        when(mockAddress.getCountry()).thenReturn("Invalid Country");
        when(mockAddress.getPostalCode()).thenReturn("TE53 3ST");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_INVALID)).thenReturn("Country Invalid");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("No errors should be produced when all fields are valid")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Country Invalid");
    }

    @Test
    void validateWhenPostCodeIsInvalid() {
        when(mockAddress.getPremises()).thenReturn("11");
        when(mockAddress.getAddressLine1()).thenReturn("Street Road");
        when(mockAddress.getAddressLine2()).thenReturn("The Lane");
        when(mockAddress.getLocality()).thenReturn("London");
        when(mockAddress.getRegion()).thenReturn("London");
        when(mockAddress.getCountry()).thenReturn("England");
        when(mockAddress.getPostalCode()).thenReturn("invalid postcode");
        when(mockApiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTCODE_UK_INVALID)).thenReturn("Postal code Invalid");

        addressValidator.validate(residentialAddressErrorProvider, mockRequest, apiErrorList, mockAddress);

        assertThat(apiErrorList)
                .as("No errors should be produced when all fields are valid")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Postal code Invalid");
    }
}