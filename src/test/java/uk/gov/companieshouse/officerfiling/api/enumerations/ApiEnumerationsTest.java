package uk.gov.companieshouse.officerfiling.api.enumerations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApiEnumerationsTest {

    private static final String ERROR_MESSAGE = "error message <1st> <2nd>";

    private ApiEnumerations apiEnumerations;

    @BeforeEach
    void setUp() {
        apiEnumerations = new ApiEnumerations(Map.of("etag-invalid", ERROR_MESSAGE), Map.of("type", "Company Type"));
    }

    @Test
    void getValidationWhenZeroCustomElements() {
        String errorMessage = apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID);
        assertThat(errorMessage)
                .isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void getValidationWhenOneCustomElements() {
        String errorMessage = apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID, "First");
        assertThat(errorMessage)
                .isEqualTo("error message First <2nd>");
    }

    @Test
    void getValidationWhenTwoCustomElements() {
        String errorMessage = apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID, "First", "Second");
        assertThat(errorMessage)
                .isEqualTo("error message First Second");
    }

    @Test
    void getCompanyType() {
        String type = apiEnumerations.getCompanyType("type");
        assertThat(type)
                .isEqualTo("Company Type");
    }

}