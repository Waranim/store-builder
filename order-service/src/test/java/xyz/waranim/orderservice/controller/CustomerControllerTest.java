package xyz.waranim.orderservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import xyz.waranim.orderservice.dto.CreateCustomerDto;
import xyz.waranim.orderservice.dto.CustomerDto;
import xyz.waranim.orderservice.service.CustomerService;

import java.util.List;
import java.util.UUID;

@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService service;

    @Test
    void testCreateCustomer() throws Exception {
        CreateCustomerDto request = new CreateCustomerDto(
                "ivan.ivanov@example.com",
                "Иван Иванов"
        );

        CustomerDto responseDto = new CustomerDto(
                UUID.randomUUID(),
                "ivan.ivanov@example.com",
                "Иван Иванов"
        );

        when(service.create(eq(request))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/order/customers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).create(request);
    }

    @Test
    void testGetCustomerById() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerDto dto = new CustomerDto(
                id,
                "john.doe@example.com",
                "John Doe"
        );

        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/order/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(dto)
                ));

        verify(service).getById(id);
    }

    @Test
    void testListAllCustomers() throws Exception {
        CustomerDto dto1 = new CustomerDto(
                UUID.randomUUID(),
                "a@example.com",
                "User A"
        );
        CustomerDto dto2 = new CustomerDto(
                UUID.randomUUID(),
                "b@example.com",
                "User B"
        );
        List<CustomerDto> list = List.of(dto1, dto2);

        when(service.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.id().toString()))
                .andExpect(jsonPath("$[1].email").value("b@example.com"));

        verify(service).getAll();
    }

    @Test
    void testUpdateCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        CreateCustomerDto request = new CreateCustomerDto(
                "updated@example.com",
                "Updated Name"
        );
        CustomerDto responseDto = new CustomerDto(
                id,
                "updated@example.com",
                "Updated Name"
        );

        when(service.update(eq(id), eq(request))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/order/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).update(id, request);
    }

    @Test
    void testDeleteCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/order/customers/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}