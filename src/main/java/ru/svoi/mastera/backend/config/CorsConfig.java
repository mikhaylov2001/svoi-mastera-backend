package ru.svoi.mastera.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "https://svoi-mastera-frontend-rose.vercel.app",
                                "https://tanstack-start-app.svoi-mastera.workers.dev",
                                "https://preview--project-code-dream.lovable.app/register",
                                "https://preview--project-code-dream.lovable.app",
                                "https://preview--project-code-dream.lovable.app/login",
                                "http://localhost:3000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("*");
            }
        };
    }
}
