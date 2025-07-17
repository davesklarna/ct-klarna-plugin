package com.e2x.klarnact.config;

import com.e2x.klarnact.commercetools.config.CommerceToolsMapperConfig;
import com.e2x.klarnact.commercetools.config.CustomLine;

import java.util.Optional;

public class CommerceToolsMapperConfigTest implements CommerceToolsMapperConfig {
    @Override
    public CustomLine customLine() {
        return () -> Optional.of("image-field-test");
    }
}
