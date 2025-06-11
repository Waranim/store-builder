package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.orderservice.entity.CartEntity;
import xyz.waranim.orderservice.entity.CartItemEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Корзина покупателя")
public record CartDto(
        @Schema(description = "UUID покупателя", example = "1e2d3c4b-5a6f-7e8d-9c0b-a1b2c3d4e5f6")
        UUID customerId,

        @Schema(description = "Позиции в корзине. Ключ карты — UUID продукта, значение — объект CartItemDto")
        Map<UUID, CartItemDto> items,

        @Schema(description = "Итоговая стоимость корзины", example = "199.90")
        BigDecimal total
) {
    public static CartDto toDto(CartEntity entity) {
        Map<UUID, CartItemDto> items = entity.getItems().values().stream()
                .collect(Collectors.toMap(
                        CartItemEntity::getProductId,
                        i -> new CartItemDto(
                                i.getProductId(),
                                i.getProductName(),
                                i.getUnitPrice(),
                                i.getQty()
                        )));
        return new CartDto(entity.getCustomerId(), items, entity.getTotal());
    }
}
