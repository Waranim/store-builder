package xyz.waranim.paymentservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.model.Payment;
import ru.loolzaaa.youkassa.pojo.Amount;
import ru.loolzaaa.youkassa.pojo.Confirmation;
import ru.loolzaaa.youkassa.pojo.Currency;
import ru.loolzaaa.youkassa.processors.PaymentProcessor;
import xyz.waranim.paymentservice.dto.CreatePaymentRequestDto;
import xyz.waranim.paymentservice.dto.PaymentResponseDto;
import xyz.waranim.paymentservice.entity.PaymentEntity;
import xyz.waranim.paymentservice.entity.ShopCredentialEntity;
import xyz.waranim.paymentservice.entity.StatusPayment;
import xyz.waranim.paymentservice.repository.PaymentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repo;
    private final CredentialService credentials;

    public PaymentResponseDto createPayment(UUID storeId, CreatePaymentRequestDto dto) {

        ApiClient client = credentials.getClient(storeId);
        PaymentProcessor pp = new PaymentProcessor(client);

        Payment youPay = pp.create(Payment.builder()
                .amount(Amount.builder().value(dto.amount().toString()).currency(Currency.RUB).build())
                .description(dto.description())
                .confirmation(Confirmation.builder()
                        .type(Confirmation.Type.REDIRECT)
                        .returnUrl(dto.returnUrl())
                        .build())
                .build(), null);

        PaymentEntity entity = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build()) // только ID, без запроса
                .yookassaPaymentId(youPay.getId())
                .amount(dto.amount())
                .status(StatusPayment.valueOf(youPay.getStatus()))
                .confirmationUrl(youPay.getConfirmation().getConfirmationUrl())
                .build();

        repo.save(entity);
        return PaymentResponseDto.toDto(entity);
    }

    public PaymentResponseDto get(UUID storeId, UUID id) {
        PaymentEntity e = repo.findById(id)
                .filter(p -> p.getShop().getStoreId().equals(storeId))
                .orElseThrow(() -> new EntityNotFoundException("Оплата не найдена"));
        return PaymentResponseDto.toDto(e);
    }

    public Page<PaymentResponseDto> list(UUID storeId, Pageable pageable) {
        return repo.findAllByShop_StoreId(storeId, pageable)
                .map(PaymentResponseDto::toDto);
    }

    public void delete(UUID storeId, UUID id) {
        PaymentEntity p = repo.findById(id)
                .filter(e -> e.getShop().getStoreId().equals(storeId))
                .orElseThrow(() -> new EntityNotFoundException("Оплата не найдена"));
        repo.delete(p);
    }
}
