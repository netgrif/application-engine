package com.netgrif.workflow.configuration;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfiguration {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private TypeResolver resolver;

    @Bean
    public Docket neaApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("engine")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .pathProvider(new RelativePathProvider(servletContext) {
                    @Override
                    public String getApplicationBasePath() {
                        return "/api";
                    }
                })
                .ignoredParameterTypes(
                        File.class, URI.class, URL.class,
                        InputStream.class, OutputStream.class, Authentication.class, Throwable.class,
                        StackTraceElement.class, IllegalArgumentException.class, ObjectNode.class, Map.class
                )
                .alternateTypeRules(
                        AlternateTypeRules.newRule(resolver.resolve(FileSystemResource.class), resolver.resolve(MultipartFile.class)),
                        AlternateTypeRules.newRule(resolver.resolve(ResponseEntity.class, resolver.resolve(Resource.class)), resolver.resolve(MultipartFile.class))
                )
                .apiInfo(info())
                .protocols(new HashSet<>(Arrays.asList("http", "https")))
                .securitySchemes(Collections.singletonList(new BasicAuth("BasicAuth")))
                .genericModelSubstitutes(PagedResources.class, ResponseEntity.class, List.class)
                .useDefaultResponseMessages(false)
                .tags(
                        new Tag("Authentication", "User authentication services"),
                        new Tag("Filter", "Persisted filters services"),
                        new Tag("Group", "Group management services"),
                        new Tag("Petri net", "Petri net management services"),
                        new Tag("Task", "Tasks management services"),
                        new Tag("User", "User management services"),
                        new Tag("Workflow", "Workflow and net's cases management services")
                )
                ;
    }

    private ApiInfo info() {
        return new ApiInfoBuilder()
                .title("Netgrif Application Engine")
                .description("Web services used in every Netgrif application engine project.")
                .contact(new Contact("NETGRIF, s.r.o.", "https://netgrif.com", "info@netgrif.com"))
                .version("4.3.0")
                .build();
    }

}
