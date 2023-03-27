package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OfficerIdentificationTypeMapperTest {

    @Test
    void testNonEEAMapping() {
        String chsType = OfficerIdentificationTypeMapper.mapIdentificationTypeToChs("N");
        assertEquals("non-eea", chsType);
    }

    @Test
    void testEEAMapping() {
        String chsType = OfficerIdentificationTypeMapper.mapIdentificationTypeToChs("Y");
        assertEquals("eea", chsType);
    }

    @Test
    void testUKLimitedCompanyMapping() {
        String chsType = OfficerIdentificationTypeMapper.mapIdentificationTypeToChs("U");
        assertEquals("uk-limited-company", chsType);
    }

    @Test
    void testOtherCorporateBodyOrFirmMapping() {
        String chsType = OfficerIdentificationTypeMapper.mapIdentificationTypeToChs("G");
        assertEquals("other-corporate-body-or-firm", chsType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z", " ", ""})
    void testNullUnrecognizedMappings(String type) {
        String chsType = OfficerIdentificationTypeMapper.mapIdentificationTypeToChs(type);
        assertNull(chsType);
    }
}
