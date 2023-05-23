package uk.gov.companieshouse.officerfiling.api.model.filing;

/**
 * Response that is sent when an officer filing POST has been processed by the API
 */
public class FilingResponse {

    private final String submissionId;
    private String name;

    public FilingResponse(String submissionId, String name) {
        this.submissionId = submissionId;
        this.name = name;
    }

    public FilingResponse(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getName() {
        return name;
    }
}
