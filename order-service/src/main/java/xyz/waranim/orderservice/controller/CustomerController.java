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
import xyz.waranim.orderservice.dto.CreateCustomerDto;
import xyz.waranim.orderservice.dto.CustomerDto;
import xyz.waranim.orderservice.service.CustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order/customers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Управление покупателями")
public class CustomerController {

    private final CustomerService service;

    @Operation(
            summary = "Создать аккаунт покупателя",
            description = "Создаёт новый аккаунт покупателя с email и полным именем"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Покупатель создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CustomerDto.class)
            )
    )
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<CustomerDto> create(@RequestBody CreateCustomerDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Operation(summary = "Получить покупателя по ID")
    @ApiResponse(responseCode = "200", description = "Данные покупателя",
            content = @Content(schema = @Schema(implementation = CustomerDto.class)))
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<CustomerDto> get(@PathVariable @Schema(description = "UUID покупателя") UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Список всех покупателей")
    @ApiResponse(responseCode = "200", description = "Список покупателей",
            content = @Content(mediaType = "application/json"))
    @GetMapping
    @Transactional
    public ResponseEntity<List<CustomerDto>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Обновить покупателя")
    @ApiResponse(responseCode = "200", description = "Покупатель обновлён",
            content = @Content(schema = @Schema(implementation = CustomerDto.class)))
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<CustomerDto> update(@PathVariable @Schema(description = "UUID покупателя") UUID id,
                                              @RequestBody CreateCustomerDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Удалить аккаунт покупателя")
    @ApiResponse(responseCode = "204", description = "Покупатель удалён")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
