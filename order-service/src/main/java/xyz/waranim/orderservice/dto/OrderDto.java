package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO заказа")
public record OrderDto(
        @Schema(description = "UUID заказа", example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,

        @Schema(description = "UUID магазина", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        UUID shopId,

        @Schema(description = "Данные клиента")
        CustomerDto customer,

        @Schema(description = "Статус заказа", example = "NEW")
        OrderStatus status,

        @Schema(description = "Итоговая сумма", example = "123.45")
        BigDecimal total,

        @Schema(description = "Позиции заказа")
        List<OrderItemDto> items
) {
    public static OrderDto of(OrderEntity entity) {
        List<OrderItemDto> items = entity.getItems().stream().map(OrderItemDto::of).toList();

        return new OrderDto(
                entity.getId(),
                entity.getShopId(),
                CustomerDto.of(entity.getCustomer()),
                entity.getStatus(),
                entity.getTotal(),
                items
        );
    }
}
