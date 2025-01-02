package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.configuration.properties.UriProperties;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    protected final ElasticsearchProperties properties;

    protected final UriProperties uriProperties;

    public ElasticsearchConfiguration(UriProperties uriProperties, ElasticsearchProperties properties) {
        this.uriProperties = uriProperties;
        this.properties = properties;
    }

    @Bean
    public String springElasticsearchReindex() {
        return properties.getReindex();
    }

    @Bean
    public String elasticPetriNetIndex() {
        return properties.getIndex().get("petriNet");
    }

    @Bean
    public String elasticCaseIndex() {
        return properties.getIndex().get("case");
    }

    @Bean
    public String elasticTaskIndex() {
        return properties.getIndex().get("task");
    }

    @Bean
    public String elasticUriIndex() {
        return uriProperties.getIndex();
    }

    @Bean
    public RestHighLevelClient restClient() {
        validateElasticsearchProperties();

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(properties.getUrl(), properties.getSearchPort(), properties.isSsl() ? "https" : "http")
        );

        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(properties.getConnectTimeout())
                        .setConnectionRequestTimeout(properties.getConnectionRequestTimeout())
                        .setSocketTimeout(properties.getSocketTimeout()));

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (properties.isIgnoreSSL()) {
                httpClientBuilder.setSSLContext(createDisabledSSLContext());
            }
            if (properties.getProxyHost() != null && properties.getProxyPort() > 0) {
                httpClientBuilder.setProxy(new HttpHost(properties.getProxyHost(), properties.getProxyPort()));
            }
            if (properties.getUsername() != null && properties.getPassword() != null) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword())
                );
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder;
        });

        return new RestHighLevelClient(builder);
    }

    protected SSLContext createDisabledSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            }, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a disabled SSL context", e);
        }
    }

    protected void validateElasticsearchProperties() {
        if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
            throw new IllegalStateException("Elasticsearch URL is not configured!");
        }
        if (properties.getSearchPort() <= 0) {
            throw new IllegalStateException("Elasticsearch search port is invalid!");
        }
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restClient());
    }

    @Bean
    public CaseEventHandler caseEventHandler() {
        return new CaseEventHandler();
    }
}
