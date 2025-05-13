package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.orderservice.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO позиции заказа")
public record OrderItemDto(
        @Schema(description = "UUID позиции заказа", example = "b234c567-d890-1234-ef56-7890abcdef12")
        UUID id,

        @Schema(description = "UUID продукта", example = "f1a2b3c4-d5e6-7f89-0123-456789abcdef")
        UUID productId,

        @Schema(description = "Имя продукта на момент заказа", example = "T-Shirt")
        String productName,

        @Schema(description = "Цена за единицу", example = "19.99")
        BigDecimal unitPrice,

        @Schema(description = "Количество единиц", example = "2")
        Integer qty
) {
    public static OrderItemDto of(OrderItemEntity entity) {
        return new OrderItemDto(
                entity.getId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getUnitPrice(),
                entity.getQty()
        );
    }
}
