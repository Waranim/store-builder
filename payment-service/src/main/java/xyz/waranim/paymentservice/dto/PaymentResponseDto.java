package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.paymentservice.entity.PaymentEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Ответ с данными платежа")
public record PaymentResponseDto(
        UUID id,
        BigDecimal amount,
        String status,
        @Schema(description = "URL, куда редиректить пользователя для оплаты")
        String confirmationUrl
) {
    public static PaymentResponseDto toDto(PaymentEntity e) {
        return new PaymentResponseDto(
                e.getId(),
                e.getAmount(),
                e.getStatus().toString(),
                e.getConfirmationUrl()
        );
    }
}
