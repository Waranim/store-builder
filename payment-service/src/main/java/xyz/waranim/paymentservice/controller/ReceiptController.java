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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.common.security.RolesAllowed;
import xyz.waranim.paymentservice.dto.CreateReceiptRequestDto;
import xyz.waranim.paymentservice.dto.ReceiptResponseDto;
import xyz.waranim.paymentservice.service.ReceiptService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{storeId}/receipts")
@Tag(name = "Receipts", description = "Операции с кассовыми чеками")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService service;

    @Operation(
            summary     = "Создать чек",
            description = "Регистрирует чек вида PAYMENT для указанного платежа."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Чек создан",
                    content = @Content(schema = @Schema(implementation = ReceiptResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    @PostMapping("/{paymentId}")
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public ResponseEntity<ReceiptResponseDto> create(
            @PathVariable UUID storeId,
            @PathVariable UUID paymentId,
            @RequestBody @Valid CreateReceiptRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createReceipt(storeId, paymentId, dto));
    }

    @Operation(summary = "Получить чек по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ReceiptResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Чек не найден")
    })
    @GetMapping("/{id}")
    @RolesAllowed({"CUSTOMER", "SELLER", "ADMIN"})
    public ReceiptResponseDto get(@PathVariable UUID storeId, @PathVariable UUID id) {
        return service.get(storeId, id);
    }

    @Operation(summary = "Список чеков магазина постранично")
    @GetMapping
    @RolesAllowed({"SELLER", "ADMIN"})
    public Page<ReceiptResponseDto> list(
            @PathVariable UUID storeId,
            @PageableDefault Pageable pageable) {

        return service.list(storeId, pageable);
    }

    @Operation(summary = "Удалить чек")
    @ApiResponse(responseCode = "204", description = "Удалён")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed({"SELLER", "ADMIN"})
    public void delete(@PathVariable UUID storeId, @PathVariable UUID id) {
        service.delete(storeId, id);
    }
}
