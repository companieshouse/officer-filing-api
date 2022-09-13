package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

public class LinksDto {
    private URI self;
    private String validationStatus;

    public LinksDto(final URI self, final String validationStatus) {
        this.self = self;
        this.validationStatus = validationStatus;
    }

    public URI getSelf() {
        return self;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LinksDto links = (LinksDto) o;
        return Objects.equals(getSelf(), links.getSelf()) &&
                Objects.equals(getValidationStatus(), links.getValidationStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSelf(), getValidationStatus());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LinksDto.class.getSimpleName() + "[", "]").add("self=" + self)
                .add("validationStatus='" + validationStatus + "'")
                .toString();
    }
}
