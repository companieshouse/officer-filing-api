package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {

    @NotNull
    private String addressLine1;
    private String addressLine2;
    private String careOf;
    private String country;
    private String locality;
    private String poBox;
    private String postalCode;
    private String premises;
    private String region;

    private Address() {
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCareOf() {
        return careOf;
    }

    public String getCountry() {
        return country;
    }

    public String getLocality() {
        return locality;
    }

    public String getPoBox() {
        return poBox;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPremises() {
        return premises;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address = (Address) o;
        return getAddressLine1().equals(address.getAddressLine1()) &&
            Objects.equals(getAddressLine2(), address.getAddressLine2()) &&
            Objects.equals(getCareOf(), address.getCareOf()) &&
            getCountry().equals(address.getCountry()) &&
            Objects.equals(getLocality(), address.getLocality()) &&
            Objects.equals(getPoBox(), address.getPoBox()) &&
            getPostalCode().equals(address.getPostalCode()) &&
            Objects.equals(getPremises(), address.getPremises()) &&
            Objects.equals(getRegion(), address.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddressLine1(), getAddressLine2(), getCareOf(), getCountry(),
            getLocality(), getPoBox(), getPostalCode(), getPremises(), getRegion());
    }

    public static Builder address() {
        return new Builder();
    }

    @JsonPOJOBuilder(buildMethodName="address", withPrefix="with")
    public static class Builder {

        List<Consumer<Address>> buildSteps;

        private Builder() {

            buildSteps = new ArrayList<>();

        }

        public Builder withAddressLine1(String value) {

            buildSteps.add(data -> data.addressLine1 = value);
            return this;
        }

        public Builder withAddressLine2(String value) {

            buildSteps.add(data -> data.addressLine2 = value);
            return this;
        }

        public Builder withCareOf(String value) {

            buildSteps.add(data -> data.careOf = value);
            return this;
        }

        public Builder withCountry(String value) {

            buildSteps.add(data -> data.country = value);
            return this;
        }

        public Builder withLocality(String value) {

            buildSteps.add(data -> data.locality = value);
            return this;
        }

        public Builder withPoBox(String value) {

            buildSteps.add(data -> data.poBox = value);
            return this;
        }

        public Builder withPostalCode(String value) {

            buildSteps.add(data -> data.postalCode = value);
            return this;
        }

        public Builder withPremises(String value) {

            buildSteps.add(data -> data.premises = value);
            return this;
        }

        public Builder withRegion(String value) {

            buildSteps.add(data -> data.region = value);
            return this;
        }

        public Address build() {

            var data = new Address();
            buildSteps.forEach(step -> step.accept(data));
            return data;
        }
    }
}
