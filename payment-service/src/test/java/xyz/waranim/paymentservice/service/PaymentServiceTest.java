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
import ru.loolzaaa.youkassa.model.Payment;
import ru.loolzaaa.youkassa.pojo.Confirmation;
import ru.loolzaaa.youkassa.processors.PaymentProcessor;
import xyz.waranim.paymentservice.dto.CreatePaymentRequestDto;
import xyz.waranim.paymentservice.dto.PaymentResponseDto;
import xyz.waranim.paymentservice.entity.PaymentEntity;
import xyz.waranim.paymentservice.entity.ShopCredentialEntity;
import xyz.waranim.paymentservice.entity.StatusPayment;
import xyz.waranim.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository repo;
    @Mock CredentialService credentials;
    @InjectMocks PaymentService service;

    UUID storeId;
    BigDecimal amount;
    String returnUrl;
    CreatePaymentRequestDto dto;

    @BeforeEach
    void init() {
        storeId   = UUID.randomUUID();
        amount    = new BigDecimal("990.00");
        dto       = new CreatePaymentRequestDto(amount, "Test purchase", "+79990000000", "https://front/pay/return");
    }

    @Test
    void createPayment_savesEntityAndReturnsDto() {
        ApiClient api = mock(ApiClient.class);
        when(credentials.getClient(storeId)).thenReturn(api);

        try (MockedConstruction<PaymentProcessor> mocked = mockConstruction(
                PaymentProcessor.class,
                (mock, ctx) -> {
                    Confirmation conf = mock(Confirmation.class);
                    when(conf.getConfirmationUrl()).thenReturn("https://pay/redirect");

                    Payment youPay = mock(Payment.class);
                    when(youPay.getId()).thenReturn("pay_1");
                    when(youPay.getStatus()).thenReturn("SUCCEEDED");
                    when(youPay.getConfirmation()).thenReturn(conf);

                    when(mock.create(any(Payment.class), isNull())).thenReturn(youPay);
                })) {

            PaymentResponseDto resp = service.createPayment(storeId, dto);

            assertEquals(amount, resp.amount());
            assertEquals("SUCCEEDED", resp.status());
            assertEquals("https://pay/redirect", resp.confirmationUrl());

            ArgumentCaptor<PaymentEntity> capt = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(repo).save(capt.capture());
            PaymentEntity saved = capt.getValue();
            assertEquals("pay_1", saved.getYookassaPaymentId());
            assertEquals(storeId, saved.getShop().getStoreId());

            assertEquals(1, mocked.constructed().size());
        }
    }

    @Test
    void get_returnsDto() {
        PaymentEntity e = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build())
                .amount(amount)
                .status(StatusPayment.SUCCEEDED)
                .confirmationUrl("url")
                .yookassaPaymentId("pay")
                .build();
        e.setId(UUID.randomUUID());

        when(repo.findById(e.getId())).thenReturn(Optional.of(e));

        PaymentResponseDto resp = service.get(storeId, e.getId());
        assertEquals(e.getAmount(), resp.amount());
    }

    @Test
    void get_wrongStore_throws() {
        UUID otherStore = UUID.randomUUID();
        PaymentEntity e = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(otherStore).build())
                .build();
        e.setId(UUID.randomUUID());
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));

        assertThrows(EntityNotFoundException.class,
                () -> service.get(storeId, e.getId()));
    }

    @Test
    void list_mapsEntitiesToDtos() {
        PaymentEntity e1 = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build())
                .amount(amount)
                .status(StatusPayment.SUCCEEDED)
                .build();
        e1.setId(UUID.randomUUID());

        PaymentEntity e2 = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build())
                .amount(new BigDecimal("1500"))
                .status(StatusPayment.CANCELED)
                .build();
        e2.setId(UUID.randomUUID());

        Page<PaymentEntity> page = new PageImpl<>(List.of(e1, e2));
        when(repo.findAllByShop_StoreId(eq(storeId), any())).thenReturn(page);

        Page<PaymentResponseDto> result = service.list(storeId, PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
        assertEquals("SUCCEEDED", result.getContent().getFirst().status());
    }

    @Test
    void delete_correctStore_deletes() {
        UUID id = UUID.randomUUID();
        PaymentEntity e = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(storeId).build())
                .build();
        e.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(e));

        service.delete(storeId, id);

        verify(repo).delete(e);
    }

    @Test
    void delete_wrongStore_throws() {
        UUID id = UUID.randomUUID();
        UUID otherStore = UUID.randomUUID();
        PaymentEntity e = PaymentEntity.builder()
                .shop(ShopCredentialEntity.builder().storeId(otherStore).build())
                .build();
        e.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(e));

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(storeId, id));
        verify(repo, never()).delete(any());
    }
}