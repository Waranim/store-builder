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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.paymentservice.dto.CreateReceiptRequestDto;
import xyz.waranim.paymentservice.dto.ReceiptItemDto;
import xyz.waranim.paymentservice.dto.ReceiptResponseDto;
import xyz.waranim.paymentservice.service.ReceiptService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReceiptController.class)
@AutoConfigureMockMvc
class ReceiptControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    ReceiptService service;

    @Test
    void createReceipt_returns201() throws Exception {
        UUID storeId   = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        CreateReceiptRequestDto req = new CreateReceiptRequestDto(
                List.of(new ReceiptItemDto("Футболка", new BigDecimal("995"), 2, 1)),
                "+79990000000",
                true);

        ReceiptResponseDto resp = new ReceiptResponseDto(
                UUID.randomUUID(), "PENDING", "rcpt_1");

        when(service.createReceipt(storeId, paymentId, req)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/{storeId}/receipts/{paymentId}", storeId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(resp)));

        verify(service).createReceipt(storeId, paymentId, req);
    }

    @Test
    void getReceipt_returnsDto() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID id      = UUID.randomUUID();

        ReceiptResponseDto dto = new ReceiptResponseDto(id, "SUCCEEDED", "rcpt_1");
        when(service.get(storeId, id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/{storeId}/receipts/{id}", storeId, id)
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        verify(service).get(storeId, id);
    }

    @Test
    void listReceipts_returnsPage() throws Exception {
        UUID storeId = UUID.randomUUID();

        ReceiptResponseDto d1 = new ReceiptResponseDto(
                UUID.randomUUID(), "SUCCEEDED", "rcpt1");
        ReceiptResponseDto d2 = new ReceiptResponseDto(
                UUID.randomUUID(), "PENDING",   "rcpt2");

        Page<ReceiptResponseDto> page =
                new PageImpl<>(List.of(d1, d2), PageRequest.of(0, 20), 2);

        when(service.list(eq(storeId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/{storeId}/receipts", storeId)
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
    void deleteReceipt_returns204() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID id      = UUID.randomUUID();

        doNothing().when(service).delete(storeId, id);

        mockMvc.perform(delete("/api/v1/{storeId}/receipts/{id}", storeId, id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(storeId, id);
    }
}