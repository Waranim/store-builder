package xyz.waranim.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.shopservice.entity.ShopEntity;

import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO магазина")
public record ShopDto(
        @Schema(description = "UUID магазина", example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,

        @Schema(description = "UUID владельца (пользователя)", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID ownerId,

        @Schema(description = "Уникальный slug магазина", example = "my-awesome-shop")
        String slug,

        @Schema(description = "Название магазина", example = "My Awesome Shop")
        String name,

        @Schema(description = "Описание магазина", example = "Магазин уникальной одежды и аксессуаров")
        String description,

        @Schema(description = "Тема магазина (JSON): цвета, шрифты и т.п.")
        Map<String, Object> theme,

        @Schema(description = "Опубликован ли магазин", example = "true")
        Boolean isPublished
) {
    public static ShopDto of(ShopEntity entity) {
        return new ShopDto(
                entity.getId(),
                entity.getOwnerId(),
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                entity.getTheme(),
                entity.getIsPublished()
        );
    }
}
