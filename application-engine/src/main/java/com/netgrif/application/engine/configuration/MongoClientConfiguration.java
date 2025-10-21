package com.netgrif.application.engine.configuration;

import com.mongodb.*;
import com.mongodb.connection.*;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableMongoRepositories(basePackages = {"com.netgrif"}, excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.netgrif\\.application\\.engine\\.module\\..*"
        )})
public class MongoClientConfiguration extends AbstractMongoClientConfiguration {

    private final DataConfigurationProperties.MongoProperties mongoProperties;

    public MongoClientConfiguration(DataConfigurationProperties.MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder
                .applyConnectionString(getConnectionString())
                .applyToConnectionPoolSettings(this::configureConnectionPoolSetting)
                .applyToSocketSettings(this::configureSocketSettings)
                .applyToSslSettings(this::configureSslSettings)
                .applyToServerSettings(this::configureServerSettings)
                .applyToClusterSettings(this::configureClusterSettings)
                .applyToLoggerSettings(this::configureLoggerSettings);
    }


    protected void configureConnectionPoolSetting(ConnectionPoolSettings.Builder builder) {
        builder.maxSize(mongoProperties.getMaxConnections())
                .minSize(mongoProperties.getMinConnections())
                .maxConnecting(mongoProperties.getMaxConnecting());

        if (mongoProperties.getConnectionIdleTimeUnit() != null) {
            builder.maxConnectionIdleTime(mongoProperties.getConnectionIdleTime(), mongoProperties.getConnectionIdleTimeUnit());
        }
        if (mongoProperties.getMaxWaitTimeUnit() != null) {
            builder.maxWaitTime(mongoProperties.getMaxWaitTime(), mongoProperties.getMaxWaitTimeUnit());
        }
    }

    protected void configureSocketSettings(SocketSettings.Builder builder) {
        if (mongoProperties.getReadTimeoutUnit() != null) {
            builder.readTimeout(mongoProperties.getReadTimeout(), mongoProperties.getReadTimeoutUnit());
        }
        if (mongoProperties.getSocketTimeoutUnit() != null) {
            builder.connectTimeout(mongoProperties.getSocketTimeout(), mongoProperties.getSocketTimeoutUnit());
        }
        builder.applyToProxySettings(this::configureProxySettings);
    }

    protected void configureProxySettings(ProxySettings.Builder builder) {
        if (!mongoProperties.isUseProxy()) {
            return;
        }
        DataConfigurationProperties.Proxy proxy = mongoProperties.getProxy();
        if (proxy != null) {
            builder.host(proxy.getHost())
                    .port(proxy.getPort());
            if (proxy.getUsername() != null && proxy.getPassword() != null) {
                builder.username(proxy.getUsername())
                        .password(proxy.getPassword());
            }
        } else if (mongoProperties.getProxyString() != null && !mongoProperties.getProxyString().isBlank()) {
            String host = parseProxyString(mongoProperties.getProxyString())[0];
            String port = parseProxyString(mongoProperties.getProxyString())[1];
            builder.host(host)
                    .port(Integer.parseInt(port));
        }
    }

    protected void configureSslSettings(SslSettings.Builder builder) {
        builder.enabled(mongoProperties.isSsl());
    }

    protected void configureClusterSettings(ClusterSettings.Builder builder) {
        if (mongoProperties.getLocalThresholdUnit() != null) {
            builder.localThreshold(mongoProperties.getLocalThreshold(), mongoProperties.getLocalThresholdUnit());
        }
        if (mongoProperties.getMode() != null) {
            builder.mode(mongoProperties.getMode());
        }
        if (mongoProperties.getReplicaSetName() != null) {
            builder.requiredReplicaSetName(mongoProperties.getReplicaSetName());
        }
        if (mongoProperties.getClusterType() != null) {
            builder.requiredClusterType(mongoProperties.getClusterType());
        }
        if (mongoProperties.getServerSelectionTimeoutUnit() != null) {
            builder.serverSelectionTimeout(mongoProperties.getServerSelectionTimeout(), mongoProperties.getServerSelectionTimeoutUnit());
        }
    }

    protected void configureLoggerSettings(LoggerSettings.Builder builder) {
        if (mongoProperties.getMaxDocumentLengthLog() >= 0) {
            builder.maxDocumentLength(mongoProperties.getMaxDocumentLengthLog());
        }
    }

    protected void configureServerSettings(ServerSettings.Builder builder) {

    }

    @NotNull
    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

    private ConnectionString getConnectionString() {
        if (mongoProperties.getUri() != null) {
            return new ConnectionString(mongoProperties.getUri());
        } else {
            StringBuilder builder = new StringBuilder("mongodb://");
            if (mongoProperties.getUsername() != null && StringUtils.hasText(mongoProperties.getUsername())) {
                builder.append(encode(mongoProperties.getUsername()));
                builder.append(":");
                if (mongoProperties.getPassword() != null && StringUtils.hasText(new String(mongoProperties.getPassword()))) {
                    builder.append(encode(mongoProperties.getPassword()));
                }

                builder.append("@");
            }

            builder.append(mongoProperties.getHost() != null ? mongoProperties.getHost() : "localhost");
            if (mongoProperties.getPort() != null) {
                builder.append(":");
                builder.append(mongoProperties.getPort());
            }

            if (mongoProperties.getAdditionalHosts() != null && !mongoProperties.getAdditionalHosts().isEmpty()) {
                builder.append(",");
                builder.append(String.join(",", mongoProperties.getAdditionalHosts()));
            }
            builder.append("/");
            builder.append(Objects.requireNonNull(mongoProperties.getMongoClientDatabase(), "Database must not be null"));
            List<String> options = getOptions();
            if (!options.isEmpty()) {
                builder.append("?");
                builder.append(String.join("&", options));
            }

            return new ConnectionString(builder.toString());
        }
    }

    private List<String> getOptions() {
        List<String> options = new ArrayList<>();
        if (mongoProperties.getUsername() != null && StringUtils.hasText(mongoProperties.getAuthenticationDatabase())) {
            options.add("authSource=" + mongoProperties.getAuthenticationDatabase());
        }
        return options;
    }

    private static String[] parseProxyString(String proxyString) {
        String[] proxyInfo = proxyString.split(":");
        if (proxyInfo.length != 2) {
            throw new IllegalArgumentException("Invalid proxy string format. Expected format: <host>:<port>");
        }
        try {
            Integer.parseInt(proxyInfo[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid proxy string format. Port must be Integer value");
        }
        return proxyInfo;
    }

    private static String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private static char[] encode(char[] input) {
        return URLEncoder.encode(new String(input), StandardCharsets.UTF_8).toCharArray();
    }
}
