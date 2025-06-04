package xyz.waranim.paymentservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.model.Receipt;
import ru.loolzaaa.youkassa.pojo.*;
import ru.loolzaaa.youkassa.processors.ReceiptProcessor;
import xyz.waranim.paymentservice.dto.CreateReceiptRequestDto;
import xyz.waranim.paymentservice.dto.ReceiptResponseDto;
import xyz.waranim.paymentservice.entity.PaymentEntity;
import xyz.waranim.paymentservice.entity.ReceiptEntity;
import xyz.waranim.paymentservice.entity.StatusReceipt;
import xyz.waranim.paymentservice.repository.PaymentRepository;
import xyz.waranim.paymentservice.repository.ReceiptRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final PaymentRepository paymentRepo;
    private final ReceiptRepository receiptRepo;
    private final CredentialService credentials;

    public ReceiptResponseDto createReceipt(UUID storeId, UUID paymentId, CreateReceiptRequestDto dto) {

        PaymentEntity payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Оплата не найдена"));

        ApiClient client = credentials.getClient(storeId);
        ReceiptProcessor rp = new ReceiptProcessor(client);

        List<Item> items = dto.items().stream()
                .map(i -> Item.builder()
                        .description(i.description())
                        .quantity(i.quantity().toString())
                        .amount(Amount.builder()
                                .value(i.price().toString())
                                .currency(Currency.RUB).build())
                        .vatCode(i.vatCode())
                        .build())
                .toList();

        Receipt youReceipt = rp.create(Receipt.builder()
                .type(Receipt.Type.PAYMENT)
                .paymentId(payment.getYookassaPaymentId())
                .customer(Customer.builder().phone(dto.phone()).build())
                .items(items)
                .send(dto.send())
                .settlements(List.of(Settlement.builder()
                        .type("cashless")
                        .amount(Amount.builder()
                                .value(payment.getAmount().toString())
                                .currency(Currency.RUB).build())
                        .build()))
                .build(), null);

        ReceiptEntity entity = ReceiptEntity.builder()
                .payment(payment)
                .yookassaReceiptId(youReceipt.getId())
                .type(youReceipt.getType())
                .status(StatusReceipt.valueOf(youReceipt.getStatus()))
                .build();

        receiptRepo.save(entity);
        return ReceiptResponseDto.toDto(entity);
    }

    public ReceiptResponseDto get(UUID storeId, UUID id) {
        ReceiptEntity r = receiptRepo.findById(id)
                .filter(rec -> rec.getPayment().getShop().getStoreId().equals(storeId))
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден"));
        return ReceiptResponseDto.toDto(r);
    }

    public Page<ReceiptResponseDto> list(UUID storeId, Pageable pageable) {
        return receiptRepo.findAllByPayment_Shop_StoreId(storeId, pageable)
                .map(ReceiptResponseDto::toDto);
    }

    public void delete(UUID storeId, UUID id) {
        ReceiptEntity r = receiptRepo.findById(id)
                .filter(rec -> rec.getPayment().getShop().getStoreId().equals(storeId))
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден"));
        receiptRepo.delete(r);
    }
}
