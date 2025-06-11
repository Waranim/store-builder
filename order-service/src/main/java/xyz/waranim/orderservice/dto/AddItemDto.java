package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Запрос: добавить товар в корзину")
public record AddItemDto(
        @NotNull
        @Schema(description = "UUID продукта", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID productId,

        @Min(1)
        @Schema(description = "Количество (⩾ 1)", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        int qty
) {
}
