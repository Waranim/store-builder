package xyz.waranim.mediaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import xyz.waranim.common.security.RolesAllowed;
import xyz.waranim.mediaservice.dto.MediaDto;
import xyz.waranim.mediaservice.entity.MediaEntity;
import xyz.waranim.mediaservice.repository.MediaRepository;
import xyz.waranim.mediaservice.service.ImageStorageService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Media", description = "Загрузка и управление изображениями")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ImageController {
    private final ImageStorageService storage;
    private final MediaRepository repo;

    @Operation(
            summary = "Загрузить изображение",
            description = "Принимает multipart‑файл через form-data, сохраняет его в MinIO и возвращает метаданные сохранённого объекта."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Файл загружен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MediaDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Файл не передан или имеет недопустимый формат")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public MediaDto upload(
            @Parameter(
                    description = "Файл изображения (jpeg, png …)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart MultipartFile file
    ) throws Exception {
        String objectKey = storage.upload(file);
        MediaEntity media = repo.save(
                new MediaEntity(objectKey,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize()));

        String url = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .build(media.getId())
                .getPath();

        return MediaDto.from(media, url);
    }

    @Operation(
            summary = "Скачать изображение",
            description = "Возвращает бинарные данные изображения по его UUID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Файл найден",
            content = @Content(mediaType = "image/*",
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<Resource> download(
            @Schema(description = "UUID изображения") @PathVariable UUID id) throws Exception {
        MediaEntity media = repo.findById(id).orElseThrow();
        byte[] data = storage.download(media.getObjectKey());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, media.getContentType())
                .body(new ByteArrayResource(data));
    }

    @Operation(
            summary = "Удалить изображение",
            description = "Удаляет файл из хранилища и метаданные из базы."
    )
    @ApiResponse(responseCode = "204", description = "Изображение удалено")
    @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    @DeleteMapping("/{id}")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public void delete(@Schema(description = "UUID изображения") @PathVariable UUID id) throws Exception {
        MediaEntity media = repo.findById(id).orElseThrow();
        storage.delete(media.getObjectKey());
        repo.delete(media);
    }
}
