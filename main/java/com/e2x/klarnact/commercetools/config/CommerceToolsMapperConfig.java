package com.e2x.klarnact.commercetools.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = CommerceToolsMapperConfig.PREFIX)
public interface CommerceToolsMapperConfig {
    String PREFIX = "commercetools-mapping";

    CustomLine customLine();
}
