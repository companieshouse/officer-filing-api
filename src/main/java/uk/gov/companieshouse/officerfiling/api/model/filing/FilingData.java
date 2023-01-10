package uk.gov.companieshouse.officerfiling.api.model.filing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilingData {

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String resignedOn;
    private String referenceEtag;

    @JsonCreator
    public FilingData(@JsonProperty("first_name") String firstName,
                      @JsonProperty("last_name") String lastName,
                      @JsonProperty("date_of_birth") String dateOfBirth,
                      @JsonProperty("resigned_on") String resignedOn,
                      @JsonProperty("referenceEtag") String referenceEtag) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.resignedOn = resignedOn;
        this.referenceEtag = referenceEtag;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getResignedOn() {
        return resignedOn;
    }

    public String getReferenceEtag() {
        return referenceEtag;
    }
}
