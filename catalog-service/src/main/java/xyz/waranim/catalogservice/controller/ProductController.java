package xyz.waranim.catalogservice.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import xyz.waranim.catalogservice.dto.CreateProduct;
import xyz.waranim.catalogservice.dto.ProductDto;
import xyz.waranim.catalogservice.dto.ProductSearchDto;
import xyz.waranim.catalogservice.service.ProductService;
import xyz.waranim.common.security.RolesAllowed;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
@Tag(name = "Products", description = "Управление товарами")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "Создать продукт",
            description = "Создаёт новый продукт для указанного магазина"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Продукт создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductDto.class)
            )
    )
    @PostMapping("/create")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<ProductDto> create(@RequestBody CreateProduct product) {
        ProductDto created = productService.create(product);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Получить продукт по ID")
    @ApiResponse(
            responseCode = "200",
            description = "Данные продукта",
            content = @Content(schema = @Schema(implementation = ProductDto.class))
    )
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductDto> get(@PathVariable @Schema(description = "UUID продукта") UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @Operation(
            summary = "Список продуктов",
            description = "Возвращает страницу продуктов по фильтру магазина и активности"
    )
    @ApiResponse(responseCode = "200", description = "Страница продуктов")
    @GetMapping
    @Transactional
    public ResponseEntity<Page<ProductDto>> list(
            @RequestParam @Schema(description = "UUID магазина для фильтра") UUID shopId,
            @RequestParam(required = false) @Schema(description = "Только активные товары") Boolean onlyActive,
            Pageable pageable) {
        return ResponseEntity.ok(productService.listByShop(shopId, onlyActive, pageable));
    }

    @Operation(
            summary = "Поиск продуктов",
            description = """
                    Выполняет поиск товаров по критериям, артикулам, тексту в названии/описании:
                    Обязательные: shopId
                    Опциональные: brandId, categoryId, onlyActive, q
                    Если указана categoryId, то будут включены продукты из этой категории и всех её подкатегорий."""
    )
    @ApiResponse(
            responseCode = "200",
            description = "Страница найденных продуктов"
    )
    @PostMapping("/search")
    @Transactional
    public ResponseEntity<Page<ProductDto>> search(
            @RequestBody
            @Schema(description = "JSON‑объект с критериями поиска") ProductSearchDto criteria,
            Pageable pageable
    ) {
        Page<ProductDto> page = productService.search(
                criteria.shopId(),
                criteria.brandId(),
                criteria.categoryId(),
                criteria.onlyActive(),
                criteria.q(),
                pageable);

        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Обновить продукт")
    @ApiResponse(
            responseCode = "200",
            description = "Продукт обновлён",
            content = @Content(schema = @Schema(implementation = ProductDto.class))
    )
    @PutMapping("/{id}")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<ProductDto> update(
            @PathVariable @Schema(description = "UUID продукта") UUID id,
            @RequestBody ProductDto product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @Operation(summary = "Обновить количество продукта")
    @ApiResponse(
            responseCode = "200",
            description = "Количество обновлено",
            content = @Content(schema = @Schema(implementation = ProductDto.class))
    )
    @PatchMapping("/{id}/{count}")
    @Transactional
    public ResponseEntity<ProductDto> updateQuantity(
            @PathVariable @Schema(description = "UUID продукта") UUID id,
            @PathVariable @Schema(description = "Новое количество продукта") Integer count) {
        return ResponseEntity.ok(productService.updateQty(id, count));
    }

    @Operation(summary = "Уменьшить количество продукта")
    @ApiResponse(
            responseCode = "200",
            description = "Количество обновлено",
            content = @Content(schema = @Schema(implementation = ProductDto.class))
    )
    @PatchMapping("/{id}/decrease/{delta}")
    @Transactional
    public ResponseEntity<ProductDto> subtractQuantity(
            @PathVariable @Schema(description = "UUID продукта") UUID id,
            @PathVariable @Schema(description = "Значение, на которое нужно уменьшить количество") Integer delta) {
        return ResponseEntity.ok(productService.subtractQuantity(id, delta));
    }

    @Operation(summary = "Удалить продукт")
    @ApiResponse(responseCode = "204", description = "Продукт удалён")
    @DeleteMapping("/{id}")
    @Transactional
    @RolesAllowed({"SELLER", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable @Schema(description = "UUID продукта") UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
