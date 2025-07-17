package com.e2x.klarnact.klarna.config;

import java.util.Optional;

public interface UserAgent {
    String platformName();

    String platformVersion();

    Optional<String> moduleName();

    Optional<String> moduleVersion();

    default String userAgentString() {
        final StringBuilder sb = new StringBuilder(platformName() + "/" + platformVersion());
        if (moduleName().isPresent() && moduleVersion().isPresent()) {
            sb.append(" ")
                    .append(moduleName().get())
                    .append("/")
                    .append(moduleVersion().get());
        }
        return sb.toString();
    }
}
