package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.time.LocalDate;
import java.util.StringJoiner;

public class TerminationDto {

    private String referenceETag;
    private String referenceOfficerId;
    private LocalDate resignedOn;

    public TerminationDto(String referenceETag, String referenceOfficerId, LocalDate resignedOn) {
        this.referenceETag = referenceETag;
        this.referenceOfficerId = referenceOfficerId;
        this.resignedOn = resignedOn;
    }

    public String getReferenceETag() {
        return referenceETag;
    }

    public String getReferenceOfficerId() {
        return referenceOfficerId;
    }

    public LocalDate getResignedOn() {
        return resignedOn;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TerminationDto.class.getSimpleName() + "[", "]").add(
                "referenceETag='" + referenceETag + "'")
            .add("referenceOfficerId='" + referenceOfficerId + "'")
            .add("resignedOn='" + resignedOn + "'")
            .toString();
    }
}
