package xyz.waranim.paymentservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.model.Receipt;
import ru.loolzaaa.youkassa.processors.ReceiptProcessor;
import xyz.waranim.paymentservice.dto.CreateReceiptRequestDto;
import xyz.waranim.paymentservice.dto.ReceiptItemDto;
import xyz.waranim.paymentservice.dto.ReceiptResponseDto;
import xyz.waranim.paymentservice.entity.*;
import xyz.waranim.paymentservice.repository.PaymentRepository;
import xyz.waranim.paymentservice.repository.ReceiptRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    PaymentRepository paymentRepo;

    @Mock
    ReceiptRepository receiptRepo;

    @Mock
    CredentialService  credentials;

    @InjectMocks
    ReceiptService service;

    UUID storeId;
    UUID paymentId;
    BigDecimal amount;

    PaymentEntity payment;

    @BeforeEach
    void init() {
        storeId   = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        amount    = new BigDecimal("1990.00");

        payment = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build())
                .yookassaPaymentId("pay_123")
                .amount(amount)
                .status(StatusPayment.SUCCEEDED)
                .build();
        payment.setId(paymentId);
    }

    @Test
    void createReceipt_savesEntityAndReturnsDto() {
        when(paymentRepo.findById(paymentId)).thenReturn(Optional.of(payment));

        ApiClient api = mock(ApiClient.class);
        when(credentials.getClient(storeId)).thenReturn(api);

        CreateReceiptRequestDto dto = new CreateReceiptRequestDto(
                List.of(new ReceiptItemDto("Футболка", new BigDecimal("995"), 2, 1)),
                "+79990000000",
                true);

        try (MockedConstruction<ReceiptProcessor> mocked = mockConstruction(
                ReceiptProcessor.class,
                (mock, ctx) -> {
                    Receipt youRec = mock(Receipt.class);
                    when(youRec.getId()).thenReturn("rcpt_1");
                    when(youRec.getStatus()).thenReturn("PENDING");
                    when(youRec.getType()).thenReturn("PAYMENT");
                    when(mock.create(any(Receipt.class), isNull())).thenReturn(youRec);
                })) {

            ReceiptResponseDto resp =
                    service.createReceipt(storeId, paymentId, dto);

            assertEquals("PENDING", resp.status());
            assertEquals("rcpt_1", resp.youkassaReceiptId());

            ArgumentCaptor<ReceiptEntity> capt = ArgumentCaptor.forClass(ReceiptEntity.class);
            verify(receiptRepo).save(capt.capture());
            ReceiptEntity saved = capt.getValue();
            assertEquals("rcpt_1", saved.getYookassaReceiptId());
            assertEquals(StatusReceipt.PENDING, saved.getStatus());
            assertSame(payment, saved.getPayment());
        }
    }

    @Test
    void createReceipt_paymentMissing_throws() {
        when(paymentRepo.findById(paymentId)).thenReturn(Optional.empty());

        CreateReceiptRequestDto dto = new CreateReceiptRequestDto(
                List.of(), "+7", true);

        assertThrows(IllegalArgumentException.class,
                () -> service.createReceipt(storeId, paymentId, dto));
        verify(receiptRepo, never()).save(any());
    }

    @Test
    void get_returnsDto() {
        ReceiptEntity r = ReceiptEntity.builder()
                .payment(payment)
                .status(StatusReceipt.SUCCEEDED)
                .yookassaReceiptId("rcpt")
                .build();
        r.setId(UUID.randomUUID());

        when(receiptRepo.findById(r.getId())).thenReturn(Optional.of(r));

        ReceiptResponseDto dto = service.get(storeId, r.getId());
        assertEquals("SUCCEEDED", dto.status());
    }

    @Test
    void get_wrongStore_throws() {
        ReceiptEntity r = ReceiptEntity.builder()
                .payment(payment)
                .status(StatusReceipt.SUCCEEDED)
                .build();
        r.setId(UUID.randomUUID());

        r.getPayment().getShop().setStoreId(UUID.randomUUID());   // чужой магазин

        when(receiptRepo.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(EntityNotFoundException.class,
                () -> service.get(storeId, r.getId()));
    }

    @Test
    void list_mapsEntitiesToDtos() {
        ReceiptEntity r = ReceiptEntity.builder()
                .payment(payment)
                .status(StatusReceipt.SUCCEEDED)
                .yookassaReceiptId("rcpt")
                .build();
        r.setId(UUID.randomUUID());

        Page<ReceiptEntity> page = new PageImpl<>(List.of(r));
        when(receiptRepo.findAllByPayment_Shop_StoreId(eq(storeId), any()))
                .thenReturn(page);

        Page<ReceiptResponseDto> result =
                service.list(storeId, PageRequest.of(0, 5));

        assertEquals(1, result.getTotalElements());
        assertEquals("SUCCEEDED", result.getContent().getFirst().status());
    }

    @Test
    void delete_correctStore_deletes() {
        UUID id = UUID.randomUUID();
        ReceiptEntity r = ReceiptEntity.builder()
                .payment(payment)
                .build();
        r.setId(id);

        when(receiptRepo.findById(id)).thenReturn(Optional.of(r));

        service.delete(storeId, id);
        verify(receiptRepo).delete(r);
    }

    @Test
    void delete_wrongStore_throws() {
        UUID id = UUID.randomUUID();
        ReceiptEntity r = ReceiptEntity.builder()
                .payment(payment)
                .build();
        r.setId(id);

        r.getPayment().getShop().setStoreId(UUID.randomUUID());

        when(receiptRepo.findById(id)).thenReturn(Optional.of(r));

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(storeId, id));
        verify(receiptRepo, never()).delete(any());
    }
}