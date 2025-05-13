package xyz.waranim.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO для создания магазина")
public record CreateShop(
        @Schema(description = "Уникальный slug магазина", example = "my-awesome-shop")
        String slug,

        @Schema(description = "Название магазина", example = "My Awesome Shop")
        String name,

        @Schema(description = "Описание магазина", example = "Магазин уникальной одежды и аксессуаров")
        String description
) {
}
