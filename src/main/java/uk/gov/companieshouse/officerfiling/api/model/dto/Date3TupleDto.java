package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.StringJoiner;

public class Date3TupleDto {
    private final int day;
    private final int month;
    private final int year;

    @JsonCreator
    public Date3TupleDto(@JsonProperty("day") final int day, @JsonProperty("month") final int month,
            @JsonProperty("year") final int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Date3TupleDto that = (Date3TupleDto) o;
        return Objects.equals(getDay(), that.getDay()) && Objects.equals(getMonth(),
                that.getMonth()) && Objects.equals(getYear(), that.getYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDay(), getMonth(), getYear());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Date3TupleDto.class.getSimpleName() + "[", "]").add(
                "day=" + day).add("month=" + month).add("year=" + year).toString();
    }
}
