package xyz.waranim.catalogservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.catalogservice.dto.CategoryDto;
import xyz.waranim.catalogservice.dto.CreateCategory;
import xyz.waranim.catalogservice.service.CategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/category")
@Tag(name="Categories", description = "CRUD категорий")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @Operation(
            summary = "Создать категорию",
            description = """
                    Создаёт новую категорию. Если <b>parentId</b> не указан,
                    категория добавляется на верхний уровень дерева."""
    )
    @ApiResponse(
            responseCode = "201",
            description = "Категория создана",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CategoryDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Родительская категория не найдена")
    @PostMapping
    @Transactional
    public ResponseEntity<CategoryDto> create(@RequestBody CreateCategory category) {
        return ResponseEntity.ok(service.create(category));
    }

    @Operation(
            summary = "Дерево категорий",
            description = "Возвращает полное иерархическое дерево категорий с вложенными подкатегориями"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список категорий",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))
    )
    @GetMapping("/tree")
    @Transactional
    public ResponseEntity<List<CategoryDto>> tree() {
        return ResponseEntity.ok(service.tree());
    }

    @Operation(
            summary = "Удалить категорию",
            description = "Удаляет категорию и рекурсивно удаляет её подкатегории"
    )
    @ApiResponse(responseCode = "204", description = "Категория удалена")
    @ApiResponse(responseCode = "404", description = "Категория не найдена")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
