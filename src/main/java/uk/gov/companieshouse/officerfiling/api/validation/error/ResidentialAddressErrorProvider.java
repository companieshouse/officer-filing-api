package uk.gov.companieshouse.officerfiling.api.validation.error;

import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;

/**
 * Provide residential-specific error messages for each address validation error
 */
public class ResidentialAddressErrorProvider implements AddressErrorProvider {

    private final ApiEnumerations apiEnumerations;

    public ResidentialAddressErrorProvider(ApiEnumerations apiEnumerations) {
        this.apiEnumerations = apiEnumerations;
    }

    @Override
    public String getPremisesBlank() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK);
    }

    @Override
    public String getPremisesLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_LENGTH);
    }

    @Override
    public String getPremisesCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_CHARACTERS);
    }

    @Override
    public String getAddressLineOneBlank() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK);
    }

    @Override
    public String getAddressLineOneLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_LENGTH);
    }

    @Override
    public String getAddressLineOneCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_CHARACTERS);
    }

    @Override
    public String getAddressLineTwoLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_LENGTH);
    }

    @Override
    public String getAddressLineTwoCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_CHARACTERS);
    }

    @Override
    public String getRegionLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_LENGTH);
    }

    @Override
    public String getRegionCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_CHARACTERS);
    }

    @Override
    public String getLocalityBlank() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK);
    }

    @Override
    public String getLocalityLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_LENGTH);
    }

    @Override
    public String getLocalityCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_CHARACTERS);
    }

    @Override
    public String getCountryBlank() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK);
    }

    @Override
    public String getCountryInvalid() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_INVALID);
    }

    @Override
    public String getCountryLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_LENGTH);
    }

    @Override
    public String getCountryCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_CHARACTERS);
    }

    @Override
    public String getPostalCodeCharacters() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_CHARACTERS);
    }

    @Override
    public String getPostalCodeLength() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_LENGTH);
    }

    @Override
    public String getPostalCodeBlank() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK);
    }

    @Override
    public String getPostalCodeUkInvalid() {
        return apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTCODE_UK_INVALID);
    }

}
