package ru.svoi.mastera.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "yookassa")
@Data
public class YooKassaProperties {
    private String shopId;
    private String secretKey;
    private String webhookSecret; // если будешь валидировать подпись
    private String returnUrl;     // https://frontend/app/payment-return
}
