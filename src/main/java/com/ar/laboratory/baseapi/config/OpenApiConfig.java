package com.ar.laboratory.baseapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.description}")
    private String appDescription;

    @Bean
    public OpenAPI customOpenAPI(@Autowired(required = false) BuildProperties buildProperties) {
        String version = buildProperties != null ? buildProperties.getVersion() : "0.0.1-SNAPSHOT";

        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .version(version)
                        .description(appDescription)
                        .contact(new Contact()
                                .name("Laboratory Team")
                                .email("contact@laboratory.ar"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
