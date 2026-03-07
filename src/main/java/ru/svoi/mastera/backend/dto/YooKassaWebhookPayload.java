package ru.svoi.mastera.backend.dto;

import lombok.Data;

@Data
public class YooKassaWebhookPayload {

    // например: "payment.succeeded", "payment.canceled"
    private String event;

    // тело объекта платежа
    private ObjectWrapper object;

    @Data
    public static class ObjectWrapper {
        private String id;                 // id платежа в ЮKassa
        private String status;             // succeeded, canceled и т.п.
        private Amount amount;             // сумма
        private Metadata metadata;         // твои доп. данные (dealId и т.п.)

        @Data
        public static class Amount {
            private String value;          // "1200.00"
            private String currency;       // "RUB"
        }

        @Data
        public static class Metadata {
            private String deal_id;        // если будешь передавать из createPayment
            private String payment_id;     // id твоей Payment, если нужно
        }
    }
}
