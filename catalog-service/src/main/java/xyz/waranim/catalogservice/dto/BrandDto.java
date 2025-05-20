package xyz.waranim.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.catalogservice.entity.BrandEntity;

import java.util.UUID;

@Schema(description = "DTO бренда")
public record BrandDto(
        @Schema(description = "UUID бренда", example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,
        @Schema(description = "Название бренда", example = "Apple")
        String name,
        @Schema(description = "URL логотипа", example = "/images/42")
        String logoUrl
) {
    public static BrandDto of(BrandEntity e) {
        return new BrandDto(e.getId(), e.getName(), e.getLogoUrl());
    }
}
