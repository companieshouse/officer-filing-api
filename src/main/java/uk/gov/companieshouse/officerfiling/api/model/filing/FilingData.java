package uk.gov.companieshouse.officerfiling.api.model.filing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilingData {

    private String referenceEtag;
    private String referenceOfficerId;
    private String resignedOn;

    @JsonCreator
    public FilingData(@JsonProperty("reference_etag") String referenceEtag,
                      @JsonProperty("reference_officer_id") String referenceOfficerId, @JsonProperty("resigned_on") String resignedOn) {
        this.referenceEtag = referenceEtag;
        this.referenceOfficerId = referenceOfficerId;
        this.resignedOn = resignedOn;
    }

    public String getReferenceEtag() {
        return referenceEtag;
    }

    public String getReferenceOfficerId() {
        return referenceOfficerId;
    }

    public String getResignedOn() {
        return resignedOn;
    }
}
