package xyz.waranim.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.paymentservice.dto.PaymentWebhookDto;
import xyz.waranim.paymentservice.dto.ReceiptWebhookDto;
import xyz.waranim.paymentservice.entity.StatusPayment;
import xyz.waranim.paymentservice.entity.StatusReceipt;
import xyz.waranim.paymentservice.repository.PaymentRepository;
import xyz.waranim.paymentservice.repository.ReceiptRepository;

@RestController
@RequestMapping("/webhooks/yookassa")
@Tag(name = "Webkooks ЮКасса",
        description = "Обработчики событий от ЮКасса (Контроллер не для фронтенда)")
@RequiredArgsConstructor
public class YookassaWebhookController {

    private final PaymentRepository paymentRepo;
    private final ReceiptRepository receiptRepo;

    @Operation(
            summary = "Webhook: обновление платежа",
            description = "Принимает события payment.succeeded, payment.canceled и др."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сохранено / пропущено"),
            @ApiResponse(responseCode = "400", description = "Неверный формат")
    })
    @PostMapping("/payment")
    @ResponseStatus(HttpStatus.OK)
    public void paymentUpdate(@RequestBody PaymentWebhookDto dto) {
        paymentRepo.findByYookassaPaymentId(dto.object().id())
                .ifPresent(p -> p.setStatus(
                        StatusPayment.valueOf(dto.object().status())));
    }

    @Operation(
            summary = "Webhook: обновление чека",
            description = "Принимает события receipt.succeeded, receipt.canceled и др."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сохранено / пропущено"),
            @ApiResponse(responseCode = "400", description = "Неверный формат")
    })
    @PostMapping("/receipt")
    @ResponseStatus(HttpStatus.OK)
    public void receiptUpdate(@RequestBody ReceiptWebhookDto dto) {
        receiptRepo.findByYookassaReceiptId(dto.object().id())
                .ifPresent(r -> r.setStatus(
                        StatusReceipt.valueOf(dto.object().status())));
    }
}
