package com.netgrif.workflow.configuration;

import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories("com.netgrif.workflow.orgstructure.domain")
public class Neo4jConfiguration {

    @Value("${spring.data.neo4j.uri}")
    private String URL;

    @Value("${spring.data.neo4j.password}")
    private String PASSWORD;

    @Value("${spring.data.neo4j.username}")
    private String USERNAME;

    @Bean
    public org.neo4j.ogm.config.Configuration getConfiguration() {
        org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
        config.driverConfiguration()
                .setDriverClassName(HttpDriver.class.getName())
                .setCredentials(USERNAME, PASSWORD)
                .setURI(URL);
        return config;
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory() {
        return new SessionFactory(getConfiguration(),"com.netgrif.workflow.orgstructure.domain");
    }
}