package uk.gov.companieshouse.officerfiling.api.validation;

import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides all validation that should be carried out when an officer is updated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerUpdateValidator extends OfficerValidator {

    private final Logger logger;
    private ApiEnumerations apiEnumerations;

    public OfficerUpdateValidator(final Logger logger,
                                  final CompanyAppointmentService companyAppointmentService,
                                  final CompanyProfileService companyProfileService,
                                  final String inputAllowedNationalities,
                                  final ApiEnumerations apiEnumerations) {
        super(logger, companyProfileService, companyAppointmentService, inputAllowedNationalities, apiEnumerations);
        this.logger = logger;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when updating an officer.
     *
     * @param request           The servlet request used in logging
     * @param dto               Data Object containing details of the update
     * @param transaction       the transaction for this update
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    @Override
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        logger.debugContext(transaction.getId(), "Beginning officer update validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();
        validateRequiredTransactionFields(request, errorList, transaction);
        validateRequiredDtoFields(request, errorList, dto);
        validateOptionalDtoFields(request, errorList, dto);

        // Retrieve data objects required for the validation process
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        final Optional<AppointmentFullRecordAPI> companyAppointment = getOfficerAppointment(request, errorList, dto, transaction, passthroughHeader);

        if (companyProfile.isEmpty() || companyAppointment.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateChangeDateAfterAppointmentDate(request, errorList, dto, companyAppointment.get());
        validateChangeDateAfterIncorporationDate(request, errorList, dto, companyProfile.get());
        validateNationalitySection(request, errorList, dto, companyAppointment.get());
        validateOccupationSection(request, errorList, dto, companyAppointment.get());
        
        return new ApiErrors(errorList);
    }

    @Override
    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateChangeDate(request, errorList, dto);
    }

    public void validateChangeDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else {
            validateChangeDatePastOrPresent(request, errorList, dto);
            validateMinChangeDate(request, errorList, dto);
        }
    }

    @Override
    public void validateOptionalDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        final boolean anyNameFieldsExistInDto = (dto.getTitle() != null || dto.getFirstName() != null || dto.getLastName() != null || dto.getMiddleNames() != null);
        final boolean nameHasBeenUpdated = (dto.getNameHasBeenUpdated() == null && anyNameFieldsExistInDto) || (dto.getNameHasBeenUpdated() != null && dto.getNameHasBeenUpdated());

        if (nameHasBeenUpdated) {
            validateTitle(request, errorList, dto);
            validateFirstName(request, errorList, dto);
            validateLastName(request, errorList, dto);
            validateMiddleNames(request, errorList, dto);
        }
    }

    public void validateChangeDatePastOrPresent(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate().isAfter(LocalDate.now())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_IN_PAST));
        }
    }

    public void validateMinChangeDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate().isBefore(MIN_RESIGNATION_DATE)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_2009));
        }
    }

    public void validateChangeDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile) {
        if (companyProfile.getDateOfCreation() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Date Of Creation field");
            return;
        }
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else if (dto.getDirectorsDetailsChangedDate().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_AFTER_INCORPORATION_DATE));
        }
    }

    public void validateChangeDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        var appointmentDate = getAppointmentDate(request, companyAppointment);
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else if (appointmentDate.isPresent() && dto.getDirectorsDetailsChangedDate().isBefore(appointmentDate.get())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_OFFICER_APPOINTMENT_DATE));
        }
    }

    public void validateNationalitySection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        // If hasBeenUpdated boolean is true or null then continue
        if (Boolean.FALSE.equals(dto.getNationalityHasBeenUpdated())) {
            return;
        }
        // If any of the fields within this section have been provided then continue
        if (dto.getNationality1() == null && dto.getNationality2() == null && dto.getNationality3() == null) {
            return;
        }
        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesNationalityMatchChipsData(dto, appointment)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.NATIONALITY_MATCHES_CHIPS_DATA));
            return;
        }
        // Perform validation
        validateNationality1(request, errorList, dto);
        validateNationality2(request, errorList, dto);
        validateNationality3(request, errorList, dto);
        validateNationalityLength(request, errorList, dto);
    }

    public void validateOccupationSection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        if (Boolean.FALSE.equals(dto.getOccupationHasBeenUpdated()) || dto.getOccupation() == null) {
            return;
        }
        if (doesOccupationMatchChipsData(dto, appointmentFullRecordAPI)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OCCUPATION_MATCHES_CHIPS_DATA));
            return;
        }
        validateOccupation(request, errorList, dto);
    }

    public boolean doesNationalityMatchChipsData(OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (appointment.getNationality() == null) {
            return false;
        }
        final String[] chipsNationalities = appointment.getNationality().split(",");

        if (!matchesChipsField(dto.getNationality1(), chipsNationalities[0])) {
            return false;
        }
        if (chipsNationalities.length < 2 && dto.getNationality2() != null) {
            return false;
        }
        if (chipsNationalities.length >= 2 && !matchesChipsField(dto.getNationality2(), chipsNationalities[1])) {
            return false;
        }
        if (chipsNationalities.length < 3 && dto.getNationality3() != null) {
            return false;
        }
        return chipsNationalities.length < 3 || matchesChipsField(dto.getNationality3(), chipsNationalities[2]);
    }

    public boolean doesOccupationMatchChipsData(OfficerFilingDto dto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        if (appointmentFullRecordAPI.getOccupation().equalsIgnoreCase("NONE") && ("NONE".equalsIgnoreCase(dto.getOccupation()) || StringUtils.isEmpty(dto.getOccupation()))) {
            return true;
        }

        final String chipsOccupation = appointmentFullRecordAPI.getOccupation();
        return matchesChipsField(dto.getOccupation(), chipsOccupation);
    }

    private boolean matchesChipsField(String field, String chipsField) {
        if (field == null && chipsField == null) {
            return true;
        } else if (field == null || chipsField == null) {
            return false;
        } else {
            return field.trim().equalsIgnoreCase(chipsField.trim());
        }
    }

}