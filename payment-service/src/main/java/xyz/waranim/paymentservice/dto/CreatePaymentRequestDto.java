package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на создание платежа")
public record CreatePaymentRequestDto(
        @Schema(description = "Сумма платежа", example = "990.00")
        @NotNull @Positive BigDecimal amount,

        @Schema(description = "Описание, показывается клиенту", example = "Premium подписка")
        String description,

        @Schema(description = "Телефон покупателя в формате +79…", example = "+79876543210")
        String phone,

        @Schema(description = "Ссылка для редиректа после оплаты", example = "example.com/successful-payment")
        String returnUrl
) {
}
