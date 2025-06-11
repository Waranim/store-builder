package xyz.waranim.paymentservice.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.paymentservice.entity.PaymentEntity;
import xyz.waranim.paymentservice.entity.ReceiptEntity;
import xyz.waranim.paymentservice.entity.StatusPayment;
import xyz.waranim.paymentservice.entity.StatusReceipt;
import xyz.waranim.paymentservice.repository.PaymentRepository;
import xyz.waranim.paymentservice.repository.ReceiptRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = YookassaWebhookController.class)
@AutoConfigureMockMvc
class YookassaWebhookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentRepository paymentRepo;

    @MockitoBean
    ReceiptRepository receiptRepo;

    @Test
    void paymentWebhook_updatesStatus() throws Exception {
        PaymentEntity entity = Mockito.mock(PaymentEntity.class);
        when(paymentRepo.findByYookassaPaymentId("pay_123"))
                .thenReturn(Optional.of(entity));

        String body = """
            {
              "event": "payment.succeeded",
              "object": {
                "id": "pay_123",
                "status": "SUCCEEDED"
              }
            }""";

        mockMvc.perform(post("/webhooks/yookassa/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(paymentRepo).findByYookassaPaymentId("pay_123");
        verify(entity).setStatus(StatusPayment.SUCCEEDED);
    }

    @Test
    void receiptWebhook_updatesStatus() throws Exception {
        ReceiptEntity entity = Mockito.mock(ReceiptEntity.class);
        when(receiptRepo.findByYookassaReceiptId("rcpt_1"))
                .thenReturn(Optional.of(entity));

        String body = """
            {
              "event": "receipt.succeeded",
              "object": {
                "id": "rcpt_1",
                "status": "SUCCEEDED"
              }
            }""";

        mockMvc.perform(post("/webhooks/yookassa/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(receiptRepo).findByYookassaReceiptId("rcpt_1");
        verify(entity).setStatus(StatusReceipt.SUCCEEDED);
    }
}