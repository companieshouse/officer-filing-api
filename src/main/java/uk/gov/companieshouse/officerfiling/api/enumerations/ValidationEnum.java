package uk.gov.companieshouse.officerfiling.api.enumerations;

/**
 * These key values reference the keys in officer_filing validation configuration within api-enumerations.
 */
public enum ValidationEnum {

    DIRECTOR_NOT_FOUND("director-not-found"),
    REMOVAL_DATE_IN_PAST("removal-date-in-past"),
    REMOVAL_DATE_MISSING("removal-date-missing"),
    DIRECTOR_ALREADY_REMOVED("director-already-removed"),
    REMOVAL_DATE_AFTER_APPOINTMENT_DATE("removal-date-after-appointment-date"),
    REMOVAL_DATE_INVALID("removal-date-invalid"),
    REMOVAL_DATE_AFTER_INCORPORATION_DATE("removal-date-after-incorporation-date"),
    REMOVAL_DATE_AFTER_2009("removal-date-after-2009"),
    OFFICER_ROLE("officer-role"),
    COMPANY_DISSOLVED("company-dissolved"),
    COMPANY_TYPE_NOT_PERMITTED("company-type-not-permitted"),
    SERVICE_UNAVAILABLE("service-unavailable"),
    CANNOT_FIND_COMPANY("cannot-find-company"),
    ETAG_INVALID("etag-invalid"),
    ETAG_BLANK("etag-blank"),
    OFFICER_ID_BLANK("officer-id-blank");

    private final String key;

    ValidationEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
