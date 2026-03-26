package com.example.hrsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// Ось ці імпорти критично важливі для фіксу:
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@SpringBootApplication
public class HrSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrSystemApplication.class, args);
    }

    // --- МАГІЧНИЙ ФІКС ДЛЯ TOMCAT ---
    // Цей код програмно знімає всі ліміти на розмір файлів і кількість полів
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // -1 означає "безліміт"
                connector.setMaxPostSize(-1);
                connector.setMaxParameterCount(-1);
            });
        };
    }
}