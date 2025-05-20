package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO создания категории")
public record CreateCategory(
        @Schema(description = "Название категории",
                example = "Фрукты",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "UUID родительской категории (опционально)",
                example = "d4e5f607-1a2b-3c4d-5e6f-708090a0b0c0")
        UUID parentId
) {
}
