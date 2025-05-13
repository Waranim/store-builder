package xyz.waranim.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderItemDto;
import xyz.waranim.orderservice.service.OrderItemService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order/items")
@Tag(name = "Order Items", description = "Управление позициями заказа")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService service;

    @Operation(summary = "Создать позицию заказа")
    @ApiResponse(responseCode = "200", description = "Позиция создана",
            content = @Content(schema = @Schema(implementation = OrderItemDto.class)))
    @PostMapping("/create")
    public ResponseEntity<OrderItemDto> create(
            @RequestParam @Schema(description = "UUID заказа") UUID orderId,
            @RequestBody CreateOrderItemDto dto
    ) {
        return ResponseEntity.ok(service.create(orderId, dto));
    }

    @Operation(summary = "Получить позицию заказа по ID")
    @ApiResponse(responseCode = "200", description = "Данные позиции",
            content = @Content(schema = @Schema(implementation = OrderItemDto.class)))
    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDto> get(@PathVariable @Schema(description = "UUID позиции заказа") UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Список позиций заказа")
    @ApiResponse(responseCode = "200", description = "Список позиций")
    @GetMapping
    public ResponseEntity<List<OrderItemDto>> list(@RequestParam @Schema(description = "UUID заказа") UUID orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId));
    }

    @Operation(summary = "Обновить позицию заказа")
    @ApiResponse(responseCode = "200", description = "Позиция обновлена",
            content = @Content(schema = @Schema(implementation = OrderItemDto.class)))
    @PutMapping("/{id}")
    public ResponseEntity<OrderItemDto> update(@PathVariable @Schema(description = "UUID позиции заказа") UUID id,
                                               @RequestBody CreateOrderItemDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Удалить позицию заказа")
    @ApiResponse(responseCode = "204", description = "Позиция удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Schema(description = "UUID позиции заказа") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
