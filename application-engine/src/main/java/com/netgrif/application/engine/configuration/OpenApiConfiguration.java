package com.netgrif.application.engine.configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ConditionalOnProperty(
        value = "netgrif.engine.openapi.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OpenApiConfiguration {

    private BuildProperties buildProperties;

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI applicationEngineOpenApi() {

        String version = (buildProperties != null)
                ? buildProperties.getVersion()
                : "7.0.0";

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("BasicAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info()
                        .title("Netgrif Application Engine")
                        .description("Web services used in every Netgrif application engine project.")
                        .version(version)
                        .license(new License()
                                .name("NETGRIF Community License")
                                .url("https://netgrif.com/license")))
                .externalDocs(new ExternalDocumentation()
                        .description("Application Engine Documentation")
                        .url("https://engine.netgrif.com"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi privateApi() {
        return GroupedOpenApi.builder()
                .group("private")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .build();
    }

}
