package xyz.waranim.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.paymentservice.entity.ReceiptEntity;

import java.util.UUID;

@Schema(description = "Ответ о зарегистрированном чеке")
public record ReceiptResponseDto(
        UUID id,
        @Schema(description = "Статус чека", example = "PENDING")
        String status,
        @Schema(description = "Идентификатор в ЮКасса", example = "28505e7c-001d-5000-9000-20093c0f42e6")
        String youkassaReceiptId
) {
    public static ReceiptResponseDto toDto(ReceiptEntity e) {
        return new ReceiptResponseDto(
                e.getId(),
                e.getStatus().name(),
                e.getYookassaReceiptId()
        );
    }
}
