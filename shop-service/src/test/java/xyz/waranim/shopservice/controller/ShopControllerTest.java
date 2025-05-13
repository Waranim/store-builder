package xyz.waranim.shopservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import xyz.waranim.shopservice.dto.CreateShop;
import xyz.waranim.shopservice.dto.ShopDto;
import xyz.waranim.shopservice.service.ShopService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebMvcTest(controllers = ShopController.class)
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShopService service;

    @Test
    void testCreateShop() throws Exception {
        String userId = UUID.randomUUID().toString();
        CreateShop request = new CreateShop(
                "my-shop-slug",
                "My Shop",
                "Shop Description"
        );

        ShopDto responseDto = new ShopDto(
                UUID.randomUUID(),
                UUID.fromString(userId),
                "my-shop-slug",
                "My Shop",
                "Shop Description",
                Map.of("themeColor", "blue"),
                true
        );

        when(service.create(eq(request), eq(userId))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/shop/create")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(responseDto)
                ));

        verify(service).create(request, userId);
    }

    @Test
    void testGetById() throws Exception {
        UUID id = UUID.randomUUID();
        ShopDto dto = new ShopDto(
                id,
                UUID.randomUUID(),
                "shop-slug",
                "Shop Name",
                "Description",
                Map.of(),
                false
        );

        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/shop/{id}", id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(dto)
                ));

        verify(service).getById(id);
    }

    @Test
    void testListAllShops() throws Exception {
        ShopDto dto1 = new ShopDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "slug1",
                "Name1",
                "Desc1",
                Map.of(),
                true
        );
        ShopDto dto2 = new ShopDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "slug2",
                "Name2",
                "Desc2",
                Map.of("key", "value"),
                false
        );
        List<ShopDto> list = List.of(dto1, dto2);

        when(service.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/v1/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.id().toString()))
                .andExpect(jsonPath("$[1].slug").value("slug2"));

        verify(service).getAll();
    }

    @Test
    void testUpdateShop() throws Exception {
        UUID id = UUID.randomUUID();
        ShopDto requestDto = new ShopDto(
                id,
                UUID.randomUUID(),
                "updated-slug",
                "Updated Name",
                "Updated Desc",
                Map.of("mode", "dark"),
                true
        );

        when(service.update(eq(id), eq(requestDto))).thenReturn(requestDto);

        mockMvc.perform(put("/api/v1/shop/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        objectMapper.writeValueAsString(requestDto)
                ));

        verify(service).update(id, requestDto);
    }

    @Test
    void testDeleteShop() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/shop/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}