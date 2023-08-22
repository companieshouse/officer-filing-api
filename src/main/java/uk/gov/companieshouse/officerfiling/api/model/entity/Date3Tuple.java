package uk.gov.companieshouse.officerfiling.api.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveDateOfBirthAPI;
import uk.gov.companieshouse.officerfiling.api.annotations.Default;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Representation of Full and Partial Dates of Birth.
 *
 * <table>
 *     <tr>
 *         <td></td> <td>Day</td> <td>Month</td> <td>Year</td>
 *     </tr>
 *     <tr>
 *         <td>Full</td> <td>o</td> <td>o</td> <td>o</td>
 *     </tr>
 *     <tr>
 *         <td>Partial</td> <td>x</td> <td>o</td> <td>o</td>
 *     </tr>
 *  <p>o = required</p>
 *  <p>x = forbidden</p>
 * </table>
 *
 */
public class Date3Tuple {
    private int day;
    private int month;
    private int year;

    /** Construct a Full/Partial Date Tuple.
     *
     * @param day the day, 0 := partial DoB
     * @param month the month
     * @param year the year
     */

    @Default
    public Date3Tuple(final int day, final int month, final int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Date3Tuple(SensitiveDateOfBirthAPI dateTime) {
        this.day = dateTime.getDay();
        this.month = dateTime.getMonth();
        this.year = dateTime.getYear();
    }

    public Date3Tuple() {

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
        final Date3Tuple that = (Date3Tuple) o;
        return Objects.equals(getDay(), that.getDay()) && Objects.equals(getMonth(),
                that.getMonth()) && Objects.equals(getYear(), that.getYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDay(), getMonth(), getYear());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Date3Tuple.class.getSimpleName() + "[", "]").add(
                "day=" + day).add("month=" + month).add("year=" + year).toString();
    }


}
