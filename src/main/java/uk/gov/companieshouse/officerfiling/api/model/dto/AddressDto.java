package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class AddressDto {

    @JsonProperty("address_line_1")
    private String addressLine1;
    @JsonProperty("address_line_2")
    private String addressLine2;
    private String careOf;
    private String country;
    private String locality;
    private String poBox;
    private String postalCode;
    private String premises;
    private String region;

    private AddressDto () {}

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Consumer<AddressDto>> buildSteps;

        public Builder() {
            this.buildSteps = new ArrayList<>();
        }

        public Builder addressLine1(String value) {

            buildSteps.add(data -> data.addressLine1 = value);
            return this;
        }

        public Builder addressLine2(String value) {

            buildSteps.add(data -> data.addressLine2 = value);
            return this;
        }

        public Builder careOf(String value) {

            buildSteps.add(data -> data.careOf = value);
            return this;
        }

        public Builder country(String value) {

            buildSteps.add(data -> data.country = value);
            return this;
        }

        public Builder locality(String value) {

            buildSteps.add(data -> data.locality = value);
            return this;
        }

        public Builder poBox(String value) {

            buildSteps.add(data -> data.poBox = value);
            return this;
        }

        public Builder postalCode(String value) {

            buildSteps.add(data -> data.postalCode = value);
            return this;
        }

        public Builder premises(String value) {

            buildSteps.add(data -> data.premises = value);
            return this;
        }

        public Builder region(String value) {

            buildSteps.add(data -> data.region = value);
            return this;
        }

        public AddressDto build() {

            AddressDto data = new AddressDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AddressDto that = (AddressDto) o;
        return Objects.equals(getAddressLine1(), that.getAddressLine1()) &&
                Objects.equals(getAddressLine2(), that.getAddressLine2()) &&
                Objects.equals(getCareOf(), that.getCareOf()) &&
                Objects.equals(getCountry(), that.getCountry()) &&
                Objects.equals(getLocality(), that.getLocality()) &&
                Objects.equals(getPoBox(), that.getPoBox()) &&
                Objects.equals(getPostalCode(), that.getPostalCode()) &&
                Objects.equals(getPremises(), that.getPremises()) &&
                Objects.equals(getRegion(), that.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddressLine1(), getAddressLine2(), getCareOf(), getCountry(),
                getLocality(), getPoBox(), getPostalCode(), getPremises(), getRegion());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AddressDto.class.getSimpleName() + "[", "]").add(
                "addressLine1='" + addressLine1 + "'")
            .add("addressLine2='" + addressLine2 + "'")
            .add("careOf='" + careOf + "'")
            .add("country='" + country + "'")
            .add("locality='" + locality + "'")
            .add("poBox='" + poBox + "'")
            .add("postalCode='" + postalCode + "'")
            .add("premises='" + premises + "'")
            .add("region='" + region + "'")
            .toString();
    }
}
