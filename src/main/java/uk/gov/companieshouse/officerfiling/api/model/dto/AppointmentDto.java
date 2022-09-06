package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AppointmentDto {


    private AddressDto address;
    private boolean addressSameAsRegisteredOfficeAddress;
    private LocalDate appointedOn;
    private String countryOfResidence;
    private Instant createdAt;
    private LocalDate dateOfBirth;
    private String eTag;
    private List<FormerNameDto> formerNames;
    private IdentificationDto identification;
    private String kind;
    private String name;
    private String nationality;
    private String occupation;
    private String officerRole;
    private String referenceOfficerListETag;
    private String status;
    private Instant updatedAt;
    private AddressDto residentialAddress;
    private boolean residentialAddressSameAsCorrespondenceAddress;

    private AppointmentDto() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Consumer<AppointmentDto>> buildSteps;

        public Builder() {
            this.buildSteps = new ArrayList<>();
        }

        public Builder address(AddressDto value) {

            buildSteps.add(data -> data.address = value);
            return this;
        }

        public Builder addressSameAsRegisteredOfficeAddress(Boolean value) {

            buildSteps.add(data -> data.addressSameAsRegisteredOfficeAddress = value);
            return this;
        }

        public Builder appointedOn(LocalDate value) {

            buildSteps.add(data -> data.appointedOn = value);
            return this;
        }

        public Builder countryOfResidence(String value) {

            buildSteps.add(data -> data.countryOfResidence = value);
            return this;
        }

        public Builder createdAt(Instant value) {

            buildSteps.add(data -> data.createdAt = value);
            return this;
        }

        public Builder dateOfBirth(LocalDate value) {

            buildSteps.add(data -> data.dateOfBirth = value);
            return this;
        }

        public Builder eTag(String value) {

            buildSteps.add(data -> data.eTag = value);
            return this;
        }

        public Builder formerNames(List<FormerNameDto> value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder identification(IdentificationDto value) {

            buildSteps.add(data -> data.identification = value);
            return this;
        }

        public Builder kind(String value) {

            buildSteps.add(data -> data.kind = value);
            return this;
        }

        public Builder name(String value) {

            buildSteps.add(data -> data.name = value);
            return this;
        }

        public Builder nationality(String value) {

            buildSteps.add(data -> data.nationality = value);
            return this;
        }

        public Builder occupation(String value) {

            buildSteps.add(data -> data.occupation = value);
            return this;
        }

        public Builder officerRole(String value) {

            buildSteps.add(data -> data.officerRole = value);
            return this;
        }

        public Builder referenceOfficerListETag(String value) {

            buildSteps.add(data -> data.referenceOfficerListETag = value);
            return this;
        }

        public Builder status(String value) {

            buildSteps.add(data -> data.status = value);
            return this;
        }

        public Builder updatedAt(Instant value) {

            buildSteps.add(data -> data.updatedAt = value);
            return this;
        }

        public Builder residentialAddress(AddressDto value) {

            buildSteps.add(data -> data.residentialAddress = value);
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
            return this;
        }

        public AppointmentDto build() {

            AppointmentDto data = new AppointmentDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
