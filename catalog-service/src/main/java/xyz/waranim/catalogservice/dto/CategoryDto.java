package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.catalogservice.entity.CategoryEntity;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO категории (дерево)")
public record CategoryDto(

        @Schema(description = "UUID категории",
                example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,

        @Schema(description = "Название категории", example = "Фрукты")
        String name,

        @Schema(description = "Список дочерних категорий")
        List<CategoryDto> children
) {
    public static CategoryDto of(CategoryEntity e) {
        return new CategoryDto(
                e.getId(),
                e.getName(),
                e.getChildren().stream()
                        .map(CategoryDto::of)
                        .toList());
    }
}
