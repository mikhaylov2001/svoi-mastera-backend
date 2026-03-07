package ru.svoi.mastera.backend.dto;

import lombok.Data;

@Data
public class YooKassaPaymentResponse {
    private String id;
    private String status;
    private Confirmation confirmation;

    @Data
    public static class Confirmation {
        private String type;
        private String confirmation_url;
    }
}
