package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Запрос: изменить количество товара")
public record UpdateItemQtyDto(

        @Min(0)
        @Schema(description = "Новое количество (0 — удалить)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
        int qty
) {
}
