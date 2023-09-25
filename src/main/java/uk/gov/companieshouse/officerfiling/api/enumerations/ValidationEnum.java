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
    PREMISES_BLANK("premises-blank"),
    PREMISES_CHARACTERS("premises-characters"),
    PREMISES_LENGTH("premises-length"),
    ADDRESS_LINE_ONE_BLANK("address-line-one-blank"),
    ADDRESS_LINE_ONE_CHARACTERS("address-line-one-characters"),
    ADDRESS_LINE_ONE_LENGTH("address-line-one-length"),
    LOCALITY_BLANK("locality-blank"),
    LOCALITY_CHARACTERS("locality-characters"),
    LOCALITY_LENGTH("locality-length"),
    COUNTRY_BLANK("country-blank"),
    COUNTRY_CHARACTERS("country-characters"),
    COUNTRY_LENGTH("country-length"),
    COUNTRY_INVALID("country-invalid"),
    POSTAL_CODE_BLANK("postal-code-blank"),
    POSTAL_CODE_CHARACTERS("postal-code-characters"),
    POSTAL_CODE_LENGTH("postal-code-length"),
    POST_CODE_INVALID("postal-code-invalid"),
    RESIDENTIAL_POSTAL_CODE_BLANK("residential-postal-code-blank"),
    RESIDENTIAL_POSTAL_CODE_CHARACTERS("residential-postal-code-characters"),
    RESIDENTIAL_POSTAL_CODE_LENGTH("residential-postal-code-length"),
    RESIDENTIAL_POST_CODE_INVALID("residential-postal-code-invalid"),
    ADDRESS_LINE_TWO_CHARACTERS("address-line-two-characters"),
    ADDRESS_LINE_TWO_LENGTH("address-line-two-length"),
    REGION_CHARACTERS("region-characters"),
    REGION_LENGTH("region-length");
    private final String key;

    ValidationEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
