package xyz.waranim.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.common.security.RolesAllowed;
import xyz.waranim.paymentservice.dto.CreatePaymentRequestDto;
import xyz.waranim.paymentservice.dto.PaymentResponseDto;
import xyz.waranim.paymentservice.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{storeId}/payments")
@Tag(name = "Payments", description = "Управление платежами")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @Operation(
            summary = "Создать платёж",
            description = "Создаёт платёж в ЮКасса, сохраняет его локально и отдаёт ссылку-redirect."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Платёж создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибочные данные",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public ResponseEntity<PaymentResponseDto> create(
            @PathVariable UUID storeId,
            @RequestBody @Valid CreatePaymentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPayment(storeId, dto));
    }

    @Operation(summary = "Получить платёж по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ОК",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    @GetMapping("/{id}")
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public PaymentResponseDto get(@PathVariable UUID storeId, @PathVariable UUID id) {
        return service.get(storeId, id);
    }

    @Operation(summary = "Список платежей магазина постранично")
    @GetMapping
    @RolesAllowed({"SELLER", "ADMIN"})
    public Page<PaymentResponseDto> list(
            @PathVariable UUID storeId,
            @PageableDefault Pageable pageable) {

        return service.list(storeId, pageable);
    }

    @Operation(summary = "Удалить платёж")
    @ApiResponse(responseCode = "204", description = "Удалён")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed({"SELLER", "ADMIN"})
    public void delete(@PathVariable UUID storeId, @PathVariable UUID id) {
        service.delete(storeId, id);
    }
}
