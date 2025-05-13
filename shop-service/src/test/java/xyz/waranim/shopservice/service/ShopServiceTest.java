package xyz.waranim.shopservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.waranim.shopservice.dto.CreateShop;
import xyz.waranim.shopservice.dto.ShopDto;
import xyz.waranim.shopservice.entity.ShopEntity;
import xyz.waranim.shopservice.repository.ShopRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;

    @Test
    void create_ShouldThrow_WhenSlugExists() {
        CreateShop dto = new CreateShop("slug", "name", "desc");
        when(shopRepository.findBySlug("slug")).thenReturn(Optional.of(new ShopEntity()));

        assertThatThrownBy(() -> shopService.create(dto, UUID.randomUUID().toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slug уже занят: slug");

        verify(shopRepository, never()).save(any());
    }

    @Test
    void create_ShouldSaveAndReturnDto_WhenSlugFree() {
        CreateShop dto = new CreateShop("slug", "name", "desc");
        when(shopRepository.findBySlug("slug")).thenReturn(Optional.empty());

        ShopEntity saved = new ShopEntity();
        saved.setId(UUID.randomUUID());
        saved.setOwnerId(UUID.randomUUID());
        saved.setSlug("slug");
        saved.setName("name");
        saved.setDescription("desc");
        when(shopRepository.save(any(ShopEntity.class))).thenReturn(saved);

        ShopDto result = shopService.create(dto, saved.getOwnerId().toString());

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.slug()).isEqualTo("slug");
        assertThat(result.name()).isEqualTo("name");
        assertThat(result.description()).isEqualTo("desc");

        ArgumentCaptor<ShopEntity> captor = ArgumentCaptor.forClass(ShopEntity.class);
        verify(shopRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("slug");
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        UUID id = UUID.randomUUID();
        ShopEntity ent = new ShopEntity();
        ent.setId(id);
        when(shopRepository.findById(id)).thenReturn(Optional.of(ent));

        ShopDto dto = shopService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(shopRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> shopService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Магазин не найден");
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        ShopEntity e1 = new ShopEntity(), e2 = new ShopEntity();
        e1.setId(UUID.randomUUID()); e2.setId(UUID.randomUUID());
        when(shopRepository.findAll()).thenReturn(List.of(e1, e2));

        List<ShopDto> list = shopService.getAll();

        assertThat(list).hasSize(2)
                .extracting(ShopDto::id)
                .containsExactly(e1.getId(), e2.getId());
    }

    @Test
    void update_ShouldModifyAndReturnDto() {
        UUID id = UUID.randomUUID();
        ShopEntity existing = new ShopEntity();
        existing.setId(id);
        when(shopRepository.findById(id)).thenReturn(Optional.of(existing));

        ShopDto incoming = new ShopDto(id, null, null,"newName", "newDesc", null, false);
        ShopEntity updated = new ShopEntity();
        updated.setId(id);
        updated.setName("newName");
        updated.setDescription("newDesc");
        updated.setTheme(null);
        updated.setIsPublished(false);
        when(shopRepository.save(existing)).thenReturn(updated);

        ShopDto result = shopService.update(id, incoming);

        assertThat(result.name()).isEqualTo("newName");
        assertThat(result.description()).isEqualTo("newDesc");
    }

    @Test
    void updateBySlug_ShouldChangeSlug() {
        UUID id = UUID.randomUUID();
        ShopEntity existing = new ShopEntity();
        existing.setId(id);
        when(shopRepository.findById(id)).thenReturn(Optional.of(existing));

        ShopEntity saved = new ShopEntity();
        saved.setId(id);
        saved.setSlug("newSlug");
        when(shopRepository.save(existing)).thenReturn(saved);

        ShopDto dto = shopService.updateBySlug(id, "newSlug");
        assertThat(dto.slug()).isEqualTo("newSlug");
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        UUID id = UUID.randomUUID();
        when(shopRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> shopService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_ShouldCallRepo_WhenExists() {
        UUID id = UUID.randomUUID();
        when(shopRepository.existsById(id)).thenReturn(true);

        shopService.delete(id);

        verify(shopRepository).deleteById(id);
    }
}