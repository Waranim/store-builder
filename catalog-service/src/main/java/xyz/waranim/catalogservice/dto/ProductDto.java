package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.catalogservice.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO продукта")
public record ProductDto(
        @Schema(description = "UUID продукта", example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,

        @Schema(description = "UUID магазина", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        UUID shopId,

        @Schema(description = "Название продукта", example = "T-Shirt")
        String name,

        @Schema(description = "Описание продукта", example = "Хлопковая футболка с принтом")
        String description,

        @Schema(description = "Цена продукта", example = "19.99")
        BigDecimal price,

        @Schema(description = "SKU продукта", example = "TS-001")
        String sku,

        @Schema(description = "URL изображения продукта",
                example = "https://example.com/images/e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab/ts-001.jpg")
        String imageUrl,

        @Schema(description = "Активен ли продукт", example = "true")
        Boolean isActive
) {
    public static ProductDto of(ProductEntity entity) {
        return new ProductDto(
                entity.getId(),
                entity.getShopId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getSku(),
                entity.getImageUrl(),
                entity.getIsActive()
        );
    }
}
