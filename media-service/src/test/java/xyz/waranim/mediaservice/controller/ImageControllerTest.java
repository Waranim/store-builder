package xyz.waranim.mediaservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import xyz.waranim.mediaservice.dto.MediaDto;
import xyz.waranim.mediaservice.entity.MediaEntity;
import xyz.waranim.mediaservice.repository.MediaRepository;
import xyz.waranim.mediaservice.service.ImageStorageService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ImageController.class)
@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ImageStorageService storage;

    @MockitoBean
    MediaRepository repo;

    @Test
    void uploadImage_returnsMediaDto() throws Exception {
        byte[] bytes = "img".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", bytes);

        String objectKey = "123-avatar.png";
        UUID mediaId = UUID.randomUUID();

        when(storage.upload(any())).thenReturn(objectKey);

        MediaEntity entity = new MediaEntity(objectKey, "avatar.png", "image/png", (long) bytes.length);
        entity.setId(mediaId);
        when(repo.save(any())).thenReturn(entity);

        String expectedUrl = "/api/v1/images/" + mediaId;
        MediaDto expectedDto = MediaDto.from(entity, expectedUrl);

        mockMvc.perform(multipart("/api/v1/images").file(file)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));

        ArgumentCaptor<MultipartFile> captor =
                ArgumentCaptor.forClass(org.springframework.web.multipart.MultipartFile.class);
        verify(storage).upload(captor.capture());
        assertThat(captor.getValue().getOriginalFilename()).isEqualTo("avatar.png");
    }

    @Test
    void downloadImage_returnsBytes() throws Exception {
        UUID id = UUID.randomUUID();
        byte[] data = "imgdata".getBytes();

        MediaEntity media = new MediaEntity("objKey", "name.jpg", "image/jpeg", (long) data.length);
        media.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(media));
        when(storage.download("objKey")).thenReturn(data);

        mockMvc.perform(get("/api/v1/images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(content().bytes(data));

        verify(storage).download("objKey");
    }

    @Test
    void deleteImage_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        MediaEntity media = new MediaEntity("key", "name", "image/png", 10L);
        media.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(media));
        doNothing().when(storage).delete("key");

        mockMvc.perform(delete("/api/v1/images/{id}", id)
                        .header("X-User-Roles", "SELLER"))
                .andExpect(status().isOk());

        verify(storage).delete("key");
        verify(repo).delete(media);
    }
}