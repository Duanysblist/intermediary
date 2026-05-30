package com.dduany.intermediary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI intermediaryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("intermediary API")
                        .description("Query and manage planning data — applications, certifications, study and fitness " +
                                "sessions, and the plan items that tie them together — through a structured REST " +
                                "interface that both humans and AI clients can consume.")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Daniel Duany")
                                .url("https://github.com/Duanysblist/intermediary")));
    }
}