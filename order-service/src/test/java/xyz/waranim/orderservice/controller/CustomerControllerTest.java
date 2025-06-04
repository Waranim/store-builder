package xyz.waranim.orderservice.controller;

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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        UUID authId = UUID.randomUUID();
        CreateCustomerDto request = new CreateCustomerDto(
                "ivan.ivanov@example.com",
                "Иван Иванов"
        );

        CustomerDto responseDto = new CustomerDto(
                UUID.randomUUID(),
                authId,
                "ivan.ivanov@example.com",
                "Иван Иванов"
        );

        when(service.create(eq(request),eq(authId))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/order/customers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", authId.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).create(request, authId);
    }

    @Test
    void testGetCustomerById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID authId = UUID.randomUUID();
        CustomerDto dto = new CustomerDto(
                id,
                authId,
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
                UUID.randomUUID(),
                "a@example.com",
                "User A"
        );
        CustomerDto dto2 = new CustomerDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "b@example.com",
                "User B"
        );
        List<CustomerDto> list = List.of(dto1, dto2);

        when(service.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/v1/order/customers")
                        .header("X-User-Roles", "SELLER"))
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
                UUID.randomUUID(),
                "updated@example.com",
                "Updated Name"
        );

        when(service.update(eq(id), eq(request))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/order/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Roles", "SELLER"))
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

        mockMvc.perform(delete("/api/v1/order/customers/{id}", id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}