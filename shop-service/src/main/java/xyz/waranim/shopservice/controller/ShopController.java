package xyz.waranim.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.shopservice.dto.CreateShop;
import xyz.waranim.shopservice.dto.ShopDto;
import xyz.waranim.shopservice.service.ShopService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shop")
@Tag(name = "Shops", description = "Управление магазинами")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService service;

    @Operation(
            summary = "Создать магазин",
            description = "Создаёт новый магазин для указанного пользователя"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Магазин создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShopDto.class)
            )
    )
    @PostMapping("/create")
    public ResponseEntity<ShopDto> create(
            @RequestBody CreateShop createShop,
            @Parameter(
                    name = "X-User-Id",
                    description = "UUID пользователя — владелец магазина",
                    required = true,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
            )
            @RequestHeader("X-User-Id") String userId) {
        ShopDto created = service.create(createShop, userId);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Получить магазин по ID")
    @ApiResponse(
            responseCode = "200",
            description = "Данные магазина",
            content = @Content(schema = @Schema(implementation = ShopDto.class))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ShopDto> get(@PathVariable @Schema(description = "UUID магазина") UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Список всех магазинов")
    @ApiResponse(responseCode = "200", description = "Список магазинов")
    @GetMapping
    public ResponseEntity<List<ShopDto>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Обновить магазин")
    @ApiResponse(
            responseCode = "200",
            description = "Магазин обновлён",
            content = @Content(schema = @Schema(implementation = ShopDto.class))
    )
    @PutMapping("/{id}")
    public ResponseEntity<ShopDto> update(
            @PathVariable @Schema(description = "UUID магазина") UUID id,
            @RequestBody ShopDto shopDto) {
        return ResponseEntity.ok(service.update(id, shopDto));
    }

    @Operation(summary = "Удалить магазин")
    @ApiResponse(responseCode = "204", description = "Магазин удалён")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Schema(description = "UUID магазина") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
