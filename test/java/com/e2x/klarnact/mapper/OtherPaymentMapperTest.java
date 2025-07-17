package com.e2x.klarnact.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OtherPaymentMapperTest {

    @Test
    void canRetrieveEnglishWhenFileAvailable() {
        final String desc = OtherPaymentMapper.getDescription("en");
        assertNotNull(desc);
        assertEquals("Non-Klarna Payment EN", desc);
    }

    @Test
    void usesDefaultIfNoConfigurationPresent() {
        final String desc = OtherPaymentMapper.getDescription("zwf");
        assertNotNull(desc);
        assertEquals(OtherPaymentMapper.DEFAULT_DESCRIPTION, desc);
    }

}
