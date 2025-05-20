package xyz.waranim.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.catalogservice.dto.CreateProduct;
import xyz.waranim.catalogservice.dto.ProductDto;
import xyz.waranim.catalogservice.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void testCreate() throws Exception {
        UUID shopId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CreateProduct request = new CreateProduct(
                shopId.toString(),
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(9.99),
                "TP-001",
                "https://example.com/image.jpg",
                brandId.toString(),
                categoryId.toString()
        );

        ProductDto responseDto = new ProductDto(
                UUID.randomUUID(),
                shopId,
                "Test Product",
                brandId,
                categoryId,
                "Test Description",
                BigDecimal.valueOf(9.99),
                "TP-001",
                "https://example.com/image.jpg",
                true
        );

        when(productService.create(request)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/product/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

        verify(productService).create(request);
    }

    @Test
    void testGetById() throws Exception {
        UUID id = UUID.randomUUID();
        ProductDto dto = new ProductDto(
                id,
                UUID.randomUUID(),
                "Test Product",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Description",
                BigDecimal.valueOf(9.99),
                "TP-001",
                "https://example.com/image.jpg",
                true
        );

        when(productService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/product/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(productService).getById(id);
    }

    @Test
    void testList() throws Exception {
        UUID shopId = UUID.randomUUID();
        ProductDto dto = new ProductDto(
                UUID.randomUUID(),
                shopId,
                "Test Product",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Description",
                BigDecimal.valueOf(9.99),
                "TP-001",
                "https://example.com/image.jpg",
                true
        );

        List<ProductDto> list = List.of(dto);
        Page<ProductDto> page = new PageImpl<>(list, PageRequest.of(0, 10), list.size());

        when(productService.listByShop(eq(shopId), eq(true), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/product")
                        .param("shopId", shopId.toString())
                        .param("onlyActive", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dto.id().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).listByShop(eq(shopId), eq(true), any(PageRequest.class));
    }

    @Test
    void testUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductDto requestDto = new ProductDto(
                id,
                shopId,
                "Updated Name",
                brandId,
                categoryId,
                "Updated Description",
                BigDecimal.valueOf(19.99),
                "UP-001",
                "https://example.com/updated.jpg",
                false
        );

        ProductDto responseDto = requestDto;

        when(productService.update(eq(id), eq(requestDto))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/product/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

        verify(productService).update(id, requestDto);
    }

    @Test
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(productService).delete(id);

        mockMvc.perform(delete("/api/v1/product/{id}", id))
                .andExpect(status().isNoContent());

        verify(productService).delete(id);
    }
}