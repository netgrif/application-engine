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


//    @Bean
//    public Docket neaApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .groupName("engine")
//                .select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.ant("/api/**"))
//                .build()
////                .pathProvider(new RelativePathProvider(servletContext) {
////                    @Override
////                    public String getApplicationBasePath() {
////                        return "/api";
////                    }
////                })
//                .ignoredParameterTypes(
//                        File.class, URI.class, URL.class,
//                        InputStream.class, OutputStream.class, Authentication.class, Throwable.class,
//                        StackTraceElement.class, IllegalArgumentException.class, ObjectNode.class, Map.class
//                )
//                .alternateTypeRules(
//                        AlternateTypeRules.newRule(resolver.resolve(FileSystemResource.class), resolver.resolve(MultipartFile.class)),
//                        AlternateTypeRules.newRule(resolver.resolve(ResponseEntity.class, resolver.resolve(Resource.class)), resolver.resolve(MultipartFile.class))
//                )
//                .apiInfo(info())
//                .protocols(new HashSet<>(Arrays.asList("http", "https")))
//                .securitySchemes(Collections.singletonList(new BasicAuth("BasicAuth")))
//                .genericModelSubstitutes(PagedModel.class, ResponseEntity.class, List.class)
//                .useDefaultResponseMessages(false)
//                .tags(
//                        new Tag("Admin console", "Administrator console"),
//                        new Tag("Authentication", "User authentication services"),
//                        new Tag("Dashboard", "Dashboard content services"),
//                        new Tag("Elasticsearch", "Elasticsearch management services"),
//                        new Tag("Filter", "Persisted filters services"),
//                        new Tag("Group", "Group management services"),
//                        new Tag("Petri net", "Petri net management services"),
//                        new Tag("Task", "Tasks management services"),
//                        new Tag("User", "User management services"),
//                        new Tag("Workflow", "Workflow and net's cases management services")
//                )
//                ;
//    }
//
//    private ApiInfo info() {
//        return new ApiInfoBuilder()
//                .title("Netgrif Application Engine")
//                .description("Web services used in every Netgrif application engine project.")
//                .contact(new Contact("NETGRIF, s.r.o.", "https://netgrif.com", "oss@netgrif.com"))
//                .version(this.projectVersion)
//                .build();
//    }

}
