package com.e2x.klarnact.commercetools.client;

import com.commercetools.api.client.ByProjectKeyRequestBuilder;
import com.commercetools.api.defaultconfig.ApiFactory;
import com.commercetools.http.okhttp4.CtOkHttp4Client;
import com.e2x.klarnact.commercetools.config.CommerceToolsConfig;
import io.vrap.rmf.base.client.ApiHttpClient;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import lombok.SneakyThrows;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.function.Function;

@ApplicationScoped
public class CtClient {

    private final CommerceToolsConfig commerceToolsConfig;
    private final ApiHttpClient httpClient;

    public CtClient(CommerceToolsConfig commerceToolsConfig) {
        this.commerceToolsConfig = commerceToolsConfig;
        this.httpClient = ApiFactory.defaultClient(
                new CtOkHttp4Client(),
                ClientCredentials.of()
                        .withClientId(commerceToolsConfig.clientId())
                        .withClientSecret(commerceToolsConfig.clientSecret())
                        .build(),
                commerceToolsConfig.authUrl(),
                commerceToolsConfig.apiUrl());
    }

    @SneakyThrows
    public <T> T request(Function<ByProjectKeyRequestBuilder, T> block) {
        return block.apply(ApiFactory.createForProject(commerceToolsConfig.projectKey(), () -> httpClient));
    }

    @PreDestroy
    @SneakyThrows
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
