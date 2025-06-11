package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Позиция в корзине")
public record CartItemDto(
        @Schema(description = "UUID продукта", example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID productId,

        @Schema(description = "Название продукта", example = "T-Shirt")
        String productName,

        @Schema(description = "Цена за единицу", example = "19.99")
        BigDecimal unitPrice,

        @Schema(description = "Количество", example = "2")
        int qty
) {
}
