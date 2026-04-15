package com.booking.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking System API")
                        .description("API for managing bookings, reservations, payments, and confirmations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Booking System Team")));
    }
}
