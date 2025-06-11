package xyz.waranim.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.catalogservice.dto.BrandDto;
import xyz.waranim.catalogservice.dto.CreateBrand;
import xyz.waranim.catalogservice.service.BrandService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BrandController.class)
@ExtendWith(MockitoExtension.class)
class BrandControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    BrandService service;

    @Test
    void createBrand_returns201() throws Exception {
        CreateBrand req = new CreateBrand("Apple", "/logo");
        BrandDto resp = new BrandDto(UUID.randomUUID(), "Apple", "/logo");

        when(service.create(eq(req))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));

        verify(service).create(req);
    }

    @Test
    void getBrandById_returnsDto() throws Exception {
        UUID id = UUID.randomUUID();
        BrandDto dto = new BrandDto(id, "Apple", "/logo");

        when(service.get(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/brand/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(service).get(id);
    }

    @Test
    void listBrands_returnsPage() throws Exception {
        BrandDto b1 = new BrandDto(UUID.randomUUID(), "Apple", "/logo1");
        BrandDto b2 = new BrandDto(UUID.randomUUID(), "Umbrella", "/logo2");

        Page<BrandDto> page = new PageImpl<>(List.of(b1, b2));
        when(service.list(PageRequest.of(0, 20))).thenReturn(page);

        mockMvc.perform(get("/api/v1/brand?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Apple"))
                .andExpect(jsonPath("$.content[1].name").value("Umbrella"));

        verify(service).list(PageRequest.of(0, 20));
    }

    @Test
    void updateBrand_returnsDto() throws Exception {
        UUID id = UUID.randomUUID();
        BrandDto incoming = new BrandDto(id, "NewName", "/newlogo");
        BrandDto updated  = new BrandDto(id, "NewName", "/newlogo");

        when(service.update(eq(id), eq(incoming))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/brand/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming))
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));

        verify(service).update(id, incoming);
    }

    @Test
    void deleteBrand_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/brand/{id}", id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}