package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.officerfiling.api.error.ErrorType;
import uk.gov.companieshouse.officerfiling.api.error.LocationType;

@Mapper(componentModel = "spring", imports = {ErrorType.class, LocationType.class})
public interface ErrorMapper {
    @Mapping(target = "error", source = "error")
    @Mapping(target="type", expression = "java(apiError.getType())")
    @Mapping(target="location", expression = "java(\"$.\" + apiError.getLocation())")
    @Mapping(target="locationType", expression = "java(LocationType.JSON_PATH.getValue())")
    ValidationStatusError map(final ApiError apiError);

    ValidationStatusError[] map(final Set<ApiError> apiErrors);
}
