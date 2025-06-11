package xyz.waranim.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.common.security.RolesAllowed;
import xyz.waranim.orderservice.dto.AddItemDto;
import xyz.waranim.orderservice.dto.CartDto;
import xyz.waranim.orderservice.dto.UpdateItemDto;
import xyz.waranim.orderservice.dto.UpdateItemQtyDto;
import xyz.waranim.orderservice.service.CartStorageService;

import java.util.UUID;

@RequestMapping("/api/v1/order/carts")
@RestController
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Carts",
        description = "Управление корзиной покупателя"
)
@RequiredArgsConstructor
public class CartController {

    private final CartStorageService cartService;

    @Operation(
            summary = "Получить корзину текущего покупателя",
            description = "Возвращает все позиции в корзине вместе с суммарной стоимостью"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Корзина найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CartDto.class)
            )
    )
    @GetMapping
    @Transactional
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public CartDto get(@RequestHeader("X-User-Id") String userId) {
        return cartService.getCart(UUID.fromString(userId));
    }

    @Operation(
            summary   = "Добавить товар в корзину",
            description = """
                    Увеличивает количество товара в корзине на указанное значение.
                    Если товара ещё нет ― создаёт новую позицию.
                    Цены и название подтягиваются автоматически из сервиса товаров.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Товар добавлен / количество увеличено",
            content = @Content(schema = @Schema(implementation = CartDto.class))
    )
    @PostMapping("/items")
    @Transactional
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public CartDto addItem(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody AddItemDto dto) {
        return cartService.addItem(UUID.fromString(userId), dto);
    }

    @Operation(
            summary = "Изменить количество товара в корзине",
            description = """
                    Устанавливает новое количество для указанного товара.
                    Можно передать qty = 0, чтобы удалить позицию (эквивалент DELETE).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Количество обновлено / позиция удалена",
            content = @Content(schema = @Schema(implementation = CartDto.class))
    )
    @PutMapping("/items/{productId}")
    @Transactional
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public CartDto updateItem(@RequestHeader("X-User-Id") String userId,
                            @PathVariable UUID productId,
                            @Valid @RequestBody UpdateItemQtyDto dto) {
        return cartService.updateItem(UUID.fromString(userId),
                new UpdateItemDto(productId, dto.qty()));
    }

    @Operation(
            summary = "Удалить товар из корзины",
            description = "Полностью убирает позицию (не зависимо от текущего количества)"
    )
    @ApiResponse(responseCode = "204", description = "Позиция удалена")
    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public void removeItem(@RequestHeader("X-User-Id") String userId, @PathVariable UUID productId) {
        cartService.removeItem(UUID.fromString(userId), productId);
    }

    @Operation(
            summary = "Очистить всю корзину",
            description = "Удаляет все товары"
    )
    @ApiResponse(responseCode = "204", description = "Корзина очищена")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public void clear(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(UUID.fromString(userId));
    }
}
