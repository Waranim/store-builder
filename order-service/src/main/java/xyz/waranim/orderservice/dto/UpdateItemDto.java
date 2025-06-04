package xyz.waranim.orderservice.dto;

import java.util.UUID;

public record UpdateItemDto(
        UUID productId,
        int qty
) {
}
