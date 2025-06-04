package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Позиция в чеке")
public record ReceiptItemDto(
        @Schema(description = "Название/описание товара", example = "Футболка Oversize")
        @NotBlank String description,

        @Schema(description = "Цена одной единицы", example = "1990.00")
        @NotNull @Positive BigDecimal price,

        @Schema(description = "Количество", example = "2")
        @NotNull @Positive Integer quantity,

        @Schema(description = "Код ставки НДС (1-6)", example = "1")
        @NotNull Integer vatCode
) {
}
