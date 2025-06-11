package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook-уведомление о платеже")
public record PaymentWebhookDto(
        @Schema(description = "Тип события", example = "payment.succeeded")
        String event,
        @Schema(description = "Тело события")
        PaymentObject object
) {
    public record PaymentObject(
            @Schema(description = "ID платежа в ЮКасса",
                    example = "2a069347-001e-5000-9000-1d7d35e43193")
            String id,
            @Schema(description = "Новый статус", example = "SUCCEEDED")
            String status
    ) { }
}
