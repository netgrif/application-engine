package com.netgrif.application.engine.configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class OpenApiConfiguration {

    @Value("${project.version}")
    private String projectVersion;

    @Bean
    public OpenAPI applicationEngineOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("BasicAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info()
                        .title("Netgrif Application Engine")
                        .description("Web services used in every Netgrif application engine project.")
                        .version(projectVersion)
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
