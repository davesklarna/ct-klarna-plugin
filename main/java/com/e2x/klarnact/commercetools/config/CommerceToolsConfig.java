package com.e2x.klarnact.commercetools.config;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = CommerceToolsConfig.PREFIX)
public interface CommerceToolsConfig {
    String PREFIX = "commercetools";

    String projectKey();

    String authUrl();

    String apiUrl();

    String clientId();

    String clientSecret();

    Optional<String> scopes();

    String orderCustomType();
}

