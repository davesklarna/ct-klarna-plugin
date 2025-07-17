package com.e2x.klarnact.config;

import com.e2x.klarnact.commercetools.config.CommerceToolsConfig;

import java.util.Optional;

public class CommerceToolsConfigTest implements CommerceToolsConfig {
    @Override
    public String projectKey() {
        return "test";
    }

    @Override
    public String authUrl() {
        return "test";
    }

    @Override
    public String apiUrl() {
        return "test";
    }

    @Override
    public String clientId() {
        return "test";
    }

    @Override
    public String clientSecret() {
        return "test";
    }

    @Override
    public Optional<String> scopes() {
        return Optional.empty();
    }

    @Override
    public String orderCustomType() {
        return "test";
    }
}
