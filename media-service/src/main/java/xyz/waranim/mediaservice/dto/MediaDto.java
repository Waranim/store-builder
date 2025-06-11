package xyz.waranim.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.mediaservice.entity.MediaEntity;

import java.util.UUID;

@Schema(description = "DTO изображения")
public record MediaDto(
        @Schema(description = "UUID изображения",
                example = "a123b456-c789-012d-ef34-567890abcdef")
        UUID id,
        @Schema(description = "URL энд‑пойнта скачивания изображения",
                example = "/api/v1/images/a123b456-c789-012d-ef34-567890abcdef")
        String url,
        @Schema(description = "Исходное имя файла", example = "avatar.png")
        String originalName,
        @Schema(description = "MIME‑тип файла", example = "image/png")
        String contentType,
        @Schema(description = "Размер файла, байт", example = "24567")
        Long size
) {
    public static MediaDto from(MediaEntity media, String url) {
        return new MediaDto(
                media.getId(), url,
                media.getOriginalName(),
                media.getContentType(),
                media.getSize()
        );
    }
}
