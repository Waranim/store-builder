package xyz.waranim.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID id,
        UUID shopId,
        String name,
        UUID brandId,
        UUID categoryId,
        String description,
        BigDecimal price,
        String sku,
        String imageUrl,
        Boolean isActive
) {
}
