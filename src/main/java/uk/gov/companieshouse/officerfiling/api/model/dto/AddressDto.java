package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

@JsonDeserialize(builder = AddressDto.Builder.class)
public class AddressDto {

    @JsonProperty("address_line_1")
    private String addressLine1;
    @JsonProperty("address_line_2")
    private String addressLine2;
    private String country;
    private String locality;
    private String poBox;
    private String postalCode;
    private String premises;
    private String region;

    private AddressDto() {
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
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

    public static Builder builder(final AddressDto other) {
        return new Builder(other);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private final List<Consumer<AddressDto>> buildSteps;

        public Builder() {
            this.buildSteps = new ArrayList<>();
        }

        public Builder(final AddressDto other) {
            this();
            this.addressLine1(other.getAddressLine1())
                    .addressLine2(other.getAddressLine2())
                    .country(other.getCountry())
                    .locality(other.getLocality())
                    .poBox(other.getPoBox())
                    .postalCode(other.getPostalCode())
                    .premises(other.getPremises())
                    .region(other.getRegion());
        }

        @JsonProperty("address_line_1")
        public Builder addressLine1(final String value) {

            buildSteps.add(data -> data.addressLine1 = value);
            return this;
        }

        @JsonProperty("address_line_2")
        public Builder addressLine2(final String value) {

            buildSteps.add(data -> data.addressLine2 = value);
            return this;
        }

        public Builder country(final String value) {

            buildSteps.add(data -> data.country = value);
            return this;
        }

        public Builder locality(final String value) {

            buildSteps.add(data -> data.locality = value);
            return this;
        }

        public Builder poBox(final String value) {

            buildSteps.add(data -> data.poBox = value);
            return this;
        }

        public Builder postalCode(final String value) {

            buildSteps.add(data -> data.postalCode = value);
            return this;
        }

        public Builder premises(final String value) {

            buildSteps.add(data -> data.premises = value);
            return this;
        }

        public Builder region(final String value) {

            buildSteps.add(data -> data.region = value);
            return this;
        }

        public AddressDto build() {

            final var data = new AddressDto();
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
        final var that = (AddressDto) o;
        return Objects.equals(getAddressLine1(), that.getAddressLine1())
                && Objects.equals(getAddressLine2(), that.getAddressLine2())
                && Objects.equals(getCountry(), that.getCountry())
                && Objects.equals(getLocality(), that.getLocality())
                && Objects.equals(getPoBox(), that.getPoBox())
                && Objects.equals(getPostalCode(), that.getPostalCode())
                && Objects.equals(getPremises(), that.getPremises())
                && Objects.equals(getRegion(), that.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddressLine1(), getAddressLine2(), getCountry(),
                getLocality(), getPoBox(), getPostalCode(), getPremises(), getRegion());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AddressDto.class.getSimpleName() + "[", "]").add(
                        "addressLine1='" + addressLine1 + "'")
                .add("addressLine2='" + addressLine2 + "'")
                .add("country='" + country + "'")
                .add("locality='" + locality + "'")
                .add("poBox='" + poBox + "'")
                .add("postalCode='" + postalCode + "'")
                .add("premises='" + premises + "'")
                .add("region='" + region + "'")
                .toString();
    }
}
