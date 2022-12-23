package uk.gov.companieshouse.officerfiling.api.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingServiceImpl;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@ExtendWith(MockitoExtension.class)
class OfficerTerminationValidatorTest {
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    public static final String TRANS_ID = "12345-54321-76666";
    private OfficerFilingService testService;

    @Mock
    private HttpServletRequest request;


    @Test
    void checkValidDatePassesRemoveOfficerValidationTest() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId("id")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        final var realError = OfficerTerminationValidator.checkExtraValidation(request, dto);
        assertTrue(realError.getErrors().isEmpty());
    }

    @Test
    void checkInValidDateFailsRemoveOfficerValidationTest() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId("id")
                .resignedOn(LocalDate.of(1022, 9, 13))
                .build();
        final var realError = OfficerTerminationValidator.checkExtraValidation(request, dto);

        assertTrue(realError.getErrors().iterator().next().toString().equals("ApiError [error=You have entered a date too far in the past. Please check the date and resubmit , errorValues=null, location=null, locationType=json-path, type=ch:validation]"));
    }
}