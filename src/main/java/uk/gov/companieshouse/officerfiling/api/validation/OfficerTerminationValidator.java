package uk.gov.companieshouse.officerfiling.api.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.error.ErrorType;
import uk.gov.companieshouse.officerfiling.api.error.LocationType;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerTerminationValidator {

    public static final LocalDate EARLIEST_POSSIBLE_DATE = LocalDate.of(2009, 10, 1);

    private final Logger logger;

    public OfficerTerminationValidator(final Logger logger) {
            this.logger = logger;
        }

    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, String transId) {
        final var logMap = LogHelper.createLogMap(transId);
        logger.debugRequest(request, "POST", logMap);
        List<ApiError> errorList = new ArrayList<>();

        // Retrieve data objects required for the validation process

        // Perform validation
        validateMinResignationDate(request, dto, errorList);

        return new ApiErrors(errorList);
    }

    private void validateMinResignationDate(HttpServletRequest request, OfficerFilingDto dto, List<ApiError> errorList) {
        // Earliest ever possible date that a director can have been removed that is valid on the CH system is the 1st of october 2009.
        if(dto.getResignedOn().isBefore(EARLIEST_POSSIBLE_DATE)) {
            final ApiError error = new ApiError("You have entered a date too far in the past. Please check the date and resubmit ",
                    request.getRequestURI(),
                    LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
            errorList.add(error);
        }
    }

}
