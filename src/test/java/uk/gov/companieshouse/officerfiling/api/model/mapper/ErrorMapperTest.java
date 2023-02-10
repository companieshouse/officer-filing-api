package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;

@ExtendWith(MockitoExtension.class)
class ErrorMapperTest {
    private ErrorMapper testMapper;

    private ApiError apiError;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(ErrorMapper.class);
        apiError = new ApiError("message", "field", "json-path", "ch:validation");
    }

    @Test
    void mapFieldError() {
        final var validationStatusError = testMapper.map(apiError);
        final var expectedError =
                new ValidationStatusError("message", "$.field", "json-path", "ch:validation");

        assertThat(validationStatusError, samePropertyValuesAs(expectedError));
    }

    @Test
    void mapFieldErrorWhenNull() {
        assertThat(testMapper.map((ApiError) null), is(nullValue()));
    }

    @Test
    void mapFieldErrorList() {
        final var validationStatusErrors = testMapper.map(Set.of(apiError));
        final var expectedError =
                new ValidationStatusError("message", "$.field", "json-path", "ch:validation");

        assertThat(validationStatusErrors,
                is(arrayContaining(samePropertyValuesAs(expectedError))));
    }

    @Test
    void mapFieldErrorListWhenNull() {
        assertThat(testMapper.map((Set<ApiError>) null), is(nullValue()));
    }
}