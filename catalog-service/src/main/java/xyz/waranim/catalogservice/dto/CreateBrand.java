package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO создания бренда")
public record CreateBrand(
        @Schema(description = "Название бренда", example = "Acme", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "URL логотипа", example = "example.com/images/42")
        String logoUrl
) {
}
