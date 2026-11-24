package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.eventlog")
public class EventLogProperties {

    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    @Data
    public static class ElasticsearchProperties {
        private String url = "http://localhost:9200";
        private String username;
        private String password;
        private String index = "nae_eventlog";

        public boolean hasCredentials() {
            return this.username != null && !this.username.isBlank() &&
                    this.password != null && !this.password.isBlank();
        }
    }

}
