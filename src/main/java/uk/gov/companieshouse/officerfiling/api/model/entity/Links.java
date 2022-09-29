package uk.gov.companieshouse.officerfiling.api.model.entity;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

public class Links {
    private final URI self;
    private final URI validationStatus;

    public Links(final URI self, final URI validationStatus) {
        this.self = self;
        this.validationStatus = validationStatus;
    }

    public URI getSelf() {
        return self;
    }

    public URI getValidationStatus() {
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
        final Links links = (Links) o;
        return Objects.equals(getSelf(), links.getSelf()) && Objects.equals(getValidationStatus(),
                links.getValidationStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSelf(), getValidationStatus());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Links.class.getSimpleName() + "[", "]").add("self=" + self)
                .add("validationStatus=" + validationStatus)
                .toString();
    }
}
