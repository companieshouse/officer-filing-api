package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.util.Objects;
import java.util.StringJoiner;

public record Date3TupleDto (int day, int month, int year) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Date3TupleDto that = (Date3TupleDto) o;
        return Objects.equals(day(), that.day()) && Objects.equals(month(),
                that.month()) && Objects.equals(year(), that.year());
    }

    @Override
    public int hashCode() {
        return Objects.hash(day(), month(), year());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Date3TupleDto.class.getSimpleName() + "[", "]").add(
                "day=" + day).add("month=" + month).add("year=" + year).toString();
    }
}
