package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.util.HashMap;
import java.util.Map;

public class OfficerIdentificationTypeMapper {

    private static Map<String, String> identificationTypesMap;
    static {
        identificationTypesMap = new HashMap<>();
        identificationTypesMap.put("N", "non-eea");
        identificationTypesMap.put("Y", "eea");
        identificationTypesMap.put("U", "uk-limited-company");
        identificationTypesMap.put("G", "other-corporate-body-or-firm");
    }

    private OfficerIdentificationTypeMapper(){}

    public static String mapIdentificationTypeToChs(String oracleIdTypeCode) {
        return identificationTypesMap.get(oracleIdTypeCode);
    }
}
