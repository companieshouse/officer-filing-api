package uk.gov.companieshouse.officerfiling.api.enumerations;

/**
 * These key values reference the keys in officer_filing validation configuration within api-enumerations.
 */
public enum ValidationEnum {

    DIRECTOR_NOT_FOUND("director-not-found"),
    REMOVAL_DATE_IN_PAST("removal-date-in-past"),
    APPOINTMENT_DATE_IN_PAST("appointment-date-in-past"),
    REMOVAL_DATE_MISSING("removal-date-missing"),
    APPOINTMENT_DATE_MISSING("appointment-date-missing"),
    DIRECTOR_ALREADY_REMOVED("director-already-removed"),
    REMOVAL_DATE_AFTER_APPOINTMENT_DATE("removal-date-after-appointment-date"),
    REMOVAL_DATE_INVALID("removal-date-invalid"),
    REMOVAL_DATE_AFTER_INCORPORATION_DATE("removal-date-after-incorporation-date"),
    APPOINTMENT_DATE_AFTER_INCORPORATION_DATE("appointment-date-after-incorporation-date"),
    REMOVAL_DATE_AFTER_2009("removal-date-after-2009"),
    OFFICER_ROLE("officer-role"),
    COMPANY_DISSOLVED("company-dissolved"),
    COMPANY_TYPE_NOT_PERMITTED("company-type-not-permitted"),
    SERVICE_UNAVAILABLE("service-unavailable"),
    CANNOT_FIND_COMPANY("cannot-find-company"),
    ETAG_INVALID("etag-invalid"),
    ETAG_BLANK("etag-blank"),
    OFFICER_ID_BLANK("officer-id-blank"),
    LAST_NAME_BLANK("last-name-blank"),
    FIRST_NAME_BLANK("first-name-blank"),
    FIRST_NAME_LENGTH("first-name-length"),
    MIDDLE_NAME_LENGTH("middle-name-length"),
    LAST_NAME_LENGTH("last-name-length"),
    TITLE_LENGTH("title-length"),
    FORMER_NAMES_LENGTH("former-names-length"),
    FIRST_NAME_CHARACTERS("first-name-characters"),
    MIDDLE_NAME_CHARACTERS("middle-name-characters"),
    LAST_NAME_CHARACTERS("last-name-characters"),
    TITLE_CHARACTERS("title-characters"),
    FORMER_NAMES_CHARACTERS("former-names-characters"),
    DATE_OF_BIRTH_BLANK("date-of-birth-blank"),
    DATE_OF_BIRTH_OVERAGE("date-of-birth-overage"),
    DATE_OF_BIRTH_UNDERAGE("date-of-birth-underage"),
    OCCUPATION_LENGTH("occupation-length"),
    OCCUPATION_CHARACTERS("occupation-characters"),
    NATIONALITY_BLANK("nationality-blank"),
    NATIONALITY_LENGTH("nationality-length"),
    NATIONALITY_LENGTH48("nationality-length48"),
    NATIONALITY_LENGTH49("nationality-length49"),
    DUPLICATE_NATIONALITY2("duplicate-nationality2"),
    DUPLICATE_NATIONALITY3("duplicate-nationality3"),
    INVALID_NATIONALITY("invalid-nationality"),
    RESIDENTIAL_PREMISES_BLANK("residential-premises-blank"),
    RESIDENTIAL_PREMISES_CHARACTERS("residential-premises-characters"),
    RESIDENTIAL_PREMISES_LENGTH("residential-premises-length"),
    RESIDENTIAL_ADDRESS_LINE_ONE_BLANK("residential-address-line-one-blank"),
    RESIDENTIAL_ADDRESS_LINE_ONE_CHARACTERS("residential-address-line-one-characters"),
    RESIDENTIAL_ADDRESS_LINE_ONE_LENGTH("residential-address-line-one-length"),
    RESIDENTIAL_LOCALITY_BLANK("residential-locality-blank"),
    RESIDENTIAL_LOCALITY_CHARACTERS("residential-locality-characters"),
    RESIDENTIAL_LOCALITY_LENGTH("residential-locality-length"),
    RESIDENTIAL_COUNTRY_BLANK("residential-country-blank"),
    RESIDENTIAL_COUNTRY_CHARACTERS("residential-country-characters"),
    RESIDENTIAL_COUNTRY_LENGTH("residential-country-length"),
    RESIDENTIAL_COUNTRY_INVALID("residential-country-invalid"),
    RESIDENTIAL_POSTAL_CODE_BLANK("residential-postal-code-blank"),
    RESIDENTIAL_POSTAL_CODE_CHARACTERS("residential-postal-code-characters"),
    RESIDENTIAL_POSTAL_CODE_LENGTH("residential-postal-code-length"),
    RESIDENTIAL_ADDRESS_LINE_TWO_CHARACTERS("residential-address-line-two-characters"),
    RESIDENTIAL_ADDRESS_LINE_TWO_LENGTH("residential-address-line-two-length"),
    RESIDENTIAL_REGION_CHARACTERS("residential-region-characters"),
    RESIDENTIAL_REGION_LENGTH("residential-region-length"),
    CORRESPONDENCE_PREMISES_BLANK("correspondence-premises-blank"),
    CORRESPONDENCE_PREMISES_CHARACTERS("correspondence-premises-characters"),
    CORRESPONDENCE_PREMISES_LENGTH("correspondence-premises-length"),
    CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK("correspondence-address-line-one-blank"),
    CORRESPONDENCE_ADDRESS_LINE_ONE_CHARACTERS("correspondence-address-line-one-characters"),
    CORRESPONDENCE_ADDRESS_LINE_ONE_LENGTH("correspondence-address-line-one-length"),
    CORRESPONDENCE_LOCALITY_BLANK("correspondence-locality-blank"),
    CORRESPONDENCE_LOCALITY_CHARACTERS("correspondence-locality-characters"),
    CORRESPONDENCE_LOCALITY_LENGTH("correspondence-locality-length"),
    CORRESPONDENCE_COUNTRY_BLANK("correspondence-country-blank"),
    CORRESPONDENCE_COUNTRY_CHARACTERS("correspondence-country-characters"),
    CORRESPONDENCE_COUNTRY_LENGTH("correspondence-country-length"),
    CORRESPONDENCE_COUNTRY_INVALID("correspondence-country-invalid"),
    CORRESPONDENCE_POSTAL_CODE_BLANK("correspondence-postal-code-blank"),
    CORRESPONDENCE_POSTAL_CODE_CHARACTERS("correspondence-postal-code-characters"),
    CORRESPONDENCE_POSTAL_CODE_LENGTH("correspondence-postal-code-length"),
    CORRESPONDENCE_ADDRESS_LINE_TWO_CHARACTERS("correspondence-address-line-two-characters"),
    CORRESPONDENCE_ADDRESS_LINE_TWO_LENGTH("correspondence-address-line-two-length"),
    CORRESPONDENCE_REGION_CHARACTERS("correspondence-region-characters"),
    CORRESPONDENCE_REGION_LENGTH("correspondence-region-length");
    private final String key;

    ValidationEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
