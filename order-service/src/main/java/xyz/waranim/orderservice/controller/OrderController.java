package xyz.waranim.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.orderservice.dto.CreateOrderDto;
import xyz.waranim.orderservice.dto.OrderDto;
import xyz.waranim.orderservice.entity.OrderStatus;
import xyz.waranim.orderservice.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order/orders")
@Tag(name = "Orders", description = "Управление заказами")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @Operation(
            summary = "Создать заказ",
            description = "Создаёт заказ с товарами"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Заказ создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderDto.class)
            )
    )
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<OrderDto> create(@RequestBody CreateOrderDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Operation(summary = "Получить заказ по ID")
    @ApiResponse(responseCode = "200", description = "Данные заказа",
            content = @Content(schema = @Schema(implementation = OrderDto.class)))
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<OrderDto> get(@PathVariable @Schema(description = "UUID заказа") UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Список заказов", description = "Можно фильтровать по shopId или customerId")
    @ApiResponse(responseCode = "200", description = "Список заказов")
    @GetMapping
    @Transactional
    public ResponseEntity<List<OrderDto>> list(
            @RequestParam(required = false) @Schema(description = "Фильтр по магазину") UUID shopId,
            @RequestParam(required = false) @Schema(description = "Фильтр по клиенту") UUID customerId
    ) {
        List<OrderDto> result;
        if (shopId != null) {
            result = service.getByShopId(shopId);
        } else if (customerId != null) {
            result = service.getByCustomerId(customerId);
        } else {
            result = service.getAll();
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Обновить статус заказа")
    @ApiResponse(responseCode = "200", description = "Статус обновлён",
            content = @Content(schema = @Schema(implementation = OrderDto.class)))
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable @Schema(description = "UUID заказа") UUID id,
            @RequestParam @Schema(description = "Новый статус заказа", example = "PAID") OrderStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @Operation(summary = "Удалить заказ")
    @ApiResponse(responseCode = "204", description = "Заказ удалён")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable @Schema(description = "UUID заказа") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
