package uk.gov.companieshouse.officerfiling.api.utils;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MapHelper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private static final Map<PropertyNamingStrategy, ObjectMapper> MAPPER_CACHE = new ConcurrentHashMap<>();

    private MapHelper() {
        // intentionally blank
    }

    /**
     * Convert an Object into a Key/Value property map.
     *
     * @param obj the Object
     * @param strategy property naming strategy used when serializing object fields
     * @return a Map of property values
     */
    public static Map<String, Object> convertObject(Object obj, PropertyNamingStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy must not be null");

        final ObjectMapper mapper = MAPPER_CACHE.computeIfAbsent(strategy, MapHelper::buildMapper);
        return mapper.convertValue(obj, MAP_TYPE);
    }

    private static ObjectMapper buildMapper(PropertyNamingStrategy strategy) {
        return JsonMapper.builder()
                .propertyNamingStrategy(strategy)
                .enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
                .build();
    }
}
