package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO для создания продукта")
public record CreateProduct(
        @Schema(description = "UUID магазина в формате строки", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        String shopId,

        @Schema(description = "Название продукта", example = "T-Shirt")
        String name,

        @Schema(description = "Описание продукта", example = "Хлопковая футболка с принтом")
        String description,

        @Schema(description = "Цена продукта", example = "19.99")
        BigDecimal price,

        @Schema(description = "SKU продукта", example = "TS-001")
        String sku,

        @Schema(description = "URL изображения продукта", example = "https://example.com/images/ts-001.jpg")
        String imageUrl,

        @Schema(description = "UUID бренда в формате строки", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        String brandId,

        @Schema(description = "UUID категории в формате строки", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        String categoryId
) {
}
