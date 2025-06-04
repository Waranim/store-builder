package xyz.waranim.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import xyz.waranim.common.kafka.OrderStatus;
import xyz.waranim.orderservice.dto.*;
import xyz.waranim.orderservice.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService service;

    @Test
    void testCreateOrder() throws Exception {
        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();
        CreateOrderItemDto item1 = new CreateOrderItemDto(
                UUID.randomUUID(),
                "Product1",
                BigDecimal.valueOf(10.0),
                2
        );
        CreateOrderItemDto item2 = new CreateOrderItemDto(
                UUID.randomUUID(),
                "Product2",
                BigDecimal.valueOf(5.5),
                1
        );
        CreateOrderDto request = new CreateOrderDto(
                shopId,
                customerId,
                List.of(item1, item2)
        );

        CustomerDto customerDto = new CustomerDto(
                customerId,
                authId,
                "customer@example.com",
                "Customer Name"
        );
        OrderItemDto dtoItem1 = new OrderItemDto(
                UUID.randomUUID(),
                item1.productId(),
                item1.productName(),
                item1.unitPrice(),
                item1.qty()
        );
        OrderItemDto dtoItem2 = new OrderItemDto(
                UUID.randomUUID(),
                item2.productId(),
                item2.productName(),
                item2.unitPrice(),
                item2.qty()
        );
        BigDecimal total = item1.unitPrice().multiply(BigDecimal.valueOf(item1.qty()))
                .add(item2.unitPrice().multiply(BigDecimal.valueOf(item2.qty())));
        OrderDto responseDto = new OrderDto(
                UUID.randomUUID(),
                shopId,
                customerDto,
                OrderStatus.NEW,
                total,
                List.of(dtoItem1, dtoItem2)
        );

        when(service.create(eq(request))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/order/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).create(request);
    }

    @Test
    void testGetOrderById() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerDto customerDto = new CustomerDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "cust@example.com",
                "Full Name"
        );
        OrderItemDto dtoItem = new OrderItemDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ItemName",
                BigDecimal.valueOf(20.0),
                3
        );
        OrderDto dto = new OrderDto(
                id,
                UUID.randomUUID(),
                customerDto,
                OrderStatus.PAID,
                BigDecimal.valueOf(60.0),
                List.of(dtoItem)
        );

        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/order/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(dto)
                ));

        verify(service).getById(id);
    }

    @Test
    void testListOrdersNoFilter() throws Exception {
        CustomerDto customerDto = new CustomerDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "cust@example.com",
                "Full Name"
        );
        OrderDto dto = new OrderDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                customerDto,
                OrderStatus.NEW,
                BigDecimal.valueOf(0.0),
                List.of()
        );
        List<OrderDto> list = List.of(dto);

        when(service.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/orders")
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.id().toString()));

        verify(service).getAll();
    }

    @Test
    void testListOrdersByShopId() throws Exception {
        UUID shopId = UUID.randomUUID();
        List<OrderDto> list = List.of();

        when(service.getByShopId(shopId)).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/orders")
                        .param("shopId", shopId.toString())
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).getByShopId(shopId);
    }

    @Test
    void testListOrdersByCustomerId() throws Exception {
        UUID customerId = UUID.randomUUID();
        List<OrderDto> list = List.of();

        when(service.getByCustomerId(customerId)).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/orders")
                        .param("customerId", customerId.toString())
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).getByCustomerId(customerId);
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        UUID id = UUID.randomUUID();
        OrderStatus newStatus = OrderStatus.PAID;
        OrderDto responseDto = new OrderDto(
                id,
                UUID.randomUUID(),
                new CustomerDto(UUID.randomUUID(), UUID.randomUUID(), "cust@example.com", "Name"),
                newStatus,
                BigDecimal.valueOf(100.0),
                List.of()
        );

        when(service.updateStatus(eq(id), eq(newStatus))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/order/orders/{id}/status", id)
                        .param("status", newStatus.name())
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).updateStatus(id, newStatus);
    }

    @Test
    void testDeleteOrder() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/order/orders/{id}", id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}