package uk.gov.companieshouse.officerfiling.api.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Map;

public final class MapHelper {

    private MapHelper() {
    }

    private static ObjectMapper mapper = null;

    public static Map<String, Object> convertObject(Object obj) {
        if (mapper == null) {
            mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }

        return mapper.convertValue(obj, new TypeReference<>() {
        });
    }

}