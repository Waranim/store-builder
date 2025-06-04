package xyz.waranim.catalogservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.catalogservice.dto.BrandDto;
import xyz.waranim.catalogservice.dto.CreateBrand;
import xyz.waranim.catalogservice.service.BrandService;
import xyz.waranim.common.security.RolesAllowed;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brand")
@Tag(name = "Brands", description = "CRUD брендов")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    @Operation(
            summary = "Создать бренд",
            description = "Создаёт новый бренд; имя должно быть уникальным"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Бренд создан",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BrandDto.class))
    )
    @ApiResponse(responseCode = "409", description = "Такое имя бренда уже существует")
    @PostMapping
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<BrandDto> create(
            @Schema(description = "Данные для нового бренда")
            @RequestBody CreateBrand req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(
            summary = "Получить бренд по ID",
            description = "Возвращает данные бренда"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Бренд найден",
            content = @Content(schema = @Schema(implementation = BrandDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Бренд не найден")
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<BrandDto> get(
            @Schema(description = "UUID бренда")
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Список брендов (постранично)",
            description = "Возвращает страницу брендов"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Страница брендов",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BrandDto.class)))
    )
    @GetMapping
    @Transactional
    public ResponseEntity<Page<BrandDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(
            summary = "Обновить бренд",
            description = "Изменяет название и/или логотип бренда"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Бренд обновлён",
            content = @Content(schema = @Schema(implementation = BrandDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Бренд не найден")
    @ApiResponse(responseCode = "409", description = "Новое имя конфликтует с существующим брендом")
    @PutMapping("/{id}")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<BrandDto> update(@Schema(description = "UUID бренда") @PathVariable UUID id,
                                           @Schema(description = "Новые данные бренда") @RequestBody BrandDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(
            summary = "Удалить бренд",
            description = "Удаляет бренд. Если к бренду привязаны товары, операция может быть запрещена (бизнес-правило)."
    )
    @ApiResponse(responseCode = "204", description = "Бренд удалён")
    @ApiResponse(responseCode = "404", description = "Бренд не найден")
    @DeleteMapping("/{id}")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<Void> delete(@Schema(description = "UUID бренда") @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
