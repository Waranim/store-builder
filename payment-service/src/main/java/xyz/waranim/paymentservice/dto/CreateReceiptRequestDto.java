package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Запрос на создание чека")
public record CreateReceiptRequestDto(
        @Schema(description = "Список товаров/услуг")
        @NotNull List<ReceiptItemDto> items,

        @Schema(description = "Телефон покупателя", example = "+79876543210")
        @NotNull String phone,

        @Schema(description = "Отправить чек сразу после регистрации", defaultValue = "true")
        Boolean send
) {
    public CreateReceiptRequestDto {
        send = send == null || send;
    }
}
