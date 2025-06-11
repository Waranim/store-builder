package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook-уведомление о чеке")
public record ReceiptWebhookDto(
        @Schema(description = "Тип события", example = "receipt.succeeded")
        String event,
        @Schema(description = "Тело события")
        ReceiptObject object
) {
    public record ReceiptObject(
            @Schema(description = "ID чека в ЮКасса",
                    example = "28505e7c-001d-5000-9000-20093c0f42e6")
            String id,
            @Schema(description = "Новый статус", example = "SUCCEEDED")
            String status
    ) { }
}
