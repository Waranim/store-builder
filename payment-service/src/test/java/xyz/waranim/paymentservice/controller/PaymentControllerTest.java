package xyz.waranim.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.paymentservice.dto.CreatePaymentRequestDto;
import xyz.waranim.paymentservice.dto.PaymentResponseDto;
import xyz.waranim.paymentservice.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    PaymentService service;

    @Test
    void createPayment_returns201AndBody() throws Exception {
        UUID storeId = UUID.randomUUID();
        CreatePaymentRequestDto req = new CreatePaymentRequestDto(
                new BigDecimal("990.00"), "Подписка", "+79990000000", "https://return.example");

        PaymentResponseDto resp = new PaymentResponseDto(
                UUID.randomUUID(), req.amount(), "SUCCEEDED", "https://pay/redirect");

        when(service.createPayment(eq(storeId), eq(req))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/{storeId}/payments", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(resp)));

        verify(service).createPayment(eq(storeId), eq(req));
    }

    @Test
    void getPayment_returnsDto() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID payId   = UUID.randomUUID();

        PaymentResponseDto dto = new PaymentResponseDto(
                payId, new BigDecimal("990.00"), "SUCCEEDED", "url");

        when(service.get(storeId, payId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/{storeId}/payments/{id}", storeId, payId)
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        verify(service).get(storeId, payId);
    }

    @Test
    void listPayments_returnsPage() throws Exception {
        UUID storeId = UUID.randomUUID();

        PaymentResponseDto d1 = new PaymentResponseDto(
                UUID.randomUUID(), new BigDecimal("100"), "SUCCEEDED", "u1");
        PaymentResponseDto d2 = new PaymentResponseDto(
                UUID.randomUUID(), new BigDecimal("200"), "PENDING",   "u2");

        Page<PaymentResponseDto> page =
                new PageImpl<>(List.of(d1, d2), PageRequest.of(0, 20), 2);

        when(service.list(eq(storeId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/{storeId}/payments", storeId)
                        .param("size", "20")
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].status").value("SUCCEEDED"));

        ArgumentCaptor<Pageable> capt = ArgumentCaptor.forClass(Pageable.class);
        verify(service).list(eq(storeId), capt.capture());
        Pageable p = capt.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(20, p.getPageSize());
    }

    @Test
    void deletePayment_returns204() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID payId   = UUID.randomUUID();

        doNothing().when(service).delete(storeId, payId);

        mockMvc.perform(delete("/api/v1/{storeId}/payments/{id}", storeId, payId)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(storeId, payId);
    }
}