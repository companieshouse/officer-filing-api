package uk.gov.companieshouse.officerfiling.api.validation.error;

/**
 * Provide individual error messages based on the type of address being validated
 */
public interface AddressErrorProvider {

    String getPremisesBlank();

    String getPremisesLength();

    String getPremisesCharacters();

    String getAddressLineOneBlank();

    String getAddressLineOneLength();

    String getAddressLineOneCharacters();

    String getAddressLineTwoLength();

    String getAddressLineTwoCharacters();

    String getRegionLength();

    String getRegionCharacters();

    String getLocalityBlank();

    String getLocalityLength();

    String getLocalityCharacters();

    String getCountryBlank();

    String getCountryInvalid();

    String getCountryLength();

    String getCountryCharacters();

    String getPostalCodeCharacters();

    String getPostalCodeLength();

    String getPostalCodeBlank();

    String getPostalCodeUkInvalid();
}