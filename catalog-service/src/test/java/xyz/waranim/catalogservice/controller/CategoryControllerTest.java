package xyz.waranim.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.catalogservice.dto.CategoryDto;
import xyz.waranim.catalogservice.dto.CreateCategory;
import xyz.waranim.catalogservice.service.CategoryService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    CategoryService service;

    @Test
    void createCategory_returnsDto() throws Exception {
        String name = "Фрукты";
        CreateCategory reqCaptured = new CreateCategory(name, null);

        CategoryDto resp = new CategoryDto(UUID.randomUUID(), name, List.of());

        when(service.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/category")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));

        ArgumentCaptor<CreateCategory> captor = ArgumentCaptor.forClass(CreateCategory.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue()).isEqualTo(reqCaptured);
    }

    @Test
    void tree_returnsHierarchy() throws Exception {
        CategoryDto root = new CategoryDto(
                UUID.randomUUID(), "Продукты",
                List.of(new CategoryDto(UUID.randomUUID(), "Фрукты", List.of())));

        when(service.tree()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/category/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Продукты"))
                .andExpect(jsonPath("$[0].children[0].name").value("Фрукты"));

        verify(service).tree();
    }

    @Test
    void deleteCategory_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/category/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}