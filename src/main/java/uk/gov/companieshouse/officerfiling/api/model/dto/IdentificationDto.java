package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.util.Objects;
import java.util.StringJoiner;

public record IdentificationDto(String identificationType, String legalAuthority, String legalForm,
                                String placeRegistered, String registrationNumber) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IdentificationDto that = (IdentificationDto) o;
        return Objects.equals(identificationType(), that.identificationType())
                && Objects.equals(legalAuthority(), that.legalAuthority())
                && Objects.equals(legalForm(), that.legalForm())
                && Objects.equals(placeRegistered(), that.placeRegistered())
                && Objects.equals(registrationNumber(), that.registrationNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificationType(), legalAuthority(), legalForm(),
                placeRegistered(), registrationNumber());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IdentificationDto.class.getSimpleName() + "[", "]").add(
                        "identificationType='" + identificationType + "'")
                .add("legalAuthority='" + legalAuthority + "'")
                .add("legalForm='" + legalForm + "'")
                .add("placeRegistered='" + placeRegistered + "'")
                .add("registrationNumber='" + registrationNumber + "'")
                .toString();
    }
}
