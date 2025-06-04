package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Критерии поиска продуктов")
public record ProductSearchDto(

        @Schema(description = "UUID магазина",
                example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID shopId,

        @Schema(description = "UUID бренда (опционально)",
                example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID brandId,

        @Schema(description = "UUID корневой категории (опционально). " +
                "Будут найдены продукты во всех подкатегориях",
                example = "d4e5f607-1a2b-3c4d-5e6f-708090a0b0c0")
        UUID categoryId,

        @Schema(description = "Флаг «только активные» (опционально)",
                example = "true")
        Boolean onlyActive,

        @Schema(description = "Текст из поисковой строки(содержит либо артикул, либо текст названия/описания товара)",
                example = "IPhone")
        String q
) {
}
