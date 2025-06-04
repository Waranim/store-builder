package xyz.waranim.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderItemDto;
import xyz.waranim.orderservice.service.OrderItemService;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = OrderItemController.class)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderItemService service;

    @Test
    void testCreateOrderItem() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreateOrderItemDto requestDto = new CreateOrderItemDto(
                UUID.randomUUID(),
                "Product Name",
                BigDecimal.valueOf(15.5),
                3
        );
        OrderItemDto responseDto = new OrderItemDto(
                UUID.randomUUID(),
                requestDto.productId(),
                requestDto.productName(),
                requestDto.unitPrice(),
                requestDto.qty()
        );

        when(service.create(eq(orderId), eq(requestDto))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/order/items/create")
                        .param("orderId", orderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).create(orderId, requestDto);
    }

    @Test
    void testGetOrderItemById() throws Exception {
        UUID id = UUID.randomUUID();
        OrderItemDto dto = new OrderItemDto(
                id,
                UUID.randomUUID(),
                "Item Name",
                BigDecimal.valueOf(20.0),
                2
        );

        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/order/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(dto)
                ));

        verify(service).getById(id);
    }

    @Test
    void testListOrderItems() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderItemDto dto1 = new OrderItemDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Name1",
                BigDecimal.valueOf(5.0),
                1
        );
        OrderItemDto dto2 = new OrderItemDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Name2",
                BigDecimal.valueOf(7.5),
                2
        );
        List<OrderItemDto> list = List.of(dto1, dto2);

        when(service.getByOrderId(orderId)).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/items")
                        .param("orderId", orderId.toString())
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.id().toString()))
                .andExpect(jsonPath("$[1].qty").value(dto2.qty()));

        verify(service).getByOrderId(orderId);
    }

    @Test
    void testUpdateOrderItem() throws Exception {
        UUID id = UUID.randomUUID();
        CreateOrderItemDto requestDto = new CreateOrderItemDto(
                UUID.randomUUID(),
                "Updated Name",
                BigDecimal.valueOf(12.0),
                4
        );
        OrderItemDto responseDto = new OrderItemDto(
                id,
                requestDto.productId(),
                requestDto.productName(),
                requestDto.unitPrice(),
                requestDto.qty()
        );

        when(service.update(eq(id), eq(requestDto))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/order/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).update(id, requestDto);
    }

    @Test
    void testDeleteOrderItem() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/order/items/{id}", id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}