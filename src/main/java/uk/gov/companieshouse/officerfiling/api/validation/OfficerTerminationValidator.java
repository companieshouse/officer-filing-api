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

public class OfficerTerminationValidator {
        private final Logger logger;

        public OfficerTerminationValidator(final Logger logger) {
            this.logger = logger;
        }


    public ApiErrors checkExtraValidation(HttpServletRequest request, OfficerFilingDto dto, String transId) {
        final var logMap = LogHelper.createLogMap(transId);

        logger.debugRequest(request, "POST", logMap);

        List<ApiError> errorList = new ArrayList<ApiError>();

        if(dto.getResignedOn().isAfter(LocalDate.of(2009, 9, 30))) {
            // Earliest ever possible date that a director can
            // have been removed that is valid on the CH system is the 1st of october 2009.
        } else {
            final ApiError error = new ApiError("You have entered a date too far in the past. Please check the date and resubmit "
                    , request.getRequestURI()
                    ,LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
            errorList.add(error);
        }

        return new ApiErrors(errorList);
    }
}
