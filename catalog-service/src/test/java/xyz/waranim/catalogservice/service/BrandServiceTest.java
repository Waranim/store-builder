package xyz.waranim.catalogservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import xyz.waranim.catalogservice.dto.BrandDto;
import xyz.waranim.catalogservice.dto.CreateBrand;
import xyz.waranim.catalogservice.entity.BrandEntity;
import xyz.waranim.catalogservice.repository.BrandRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    BrandRepository repo;

    @InjectMocks
    BrandService service;

    private BrandEntity entity(UUID id, String name) {
        BrandEntity e = new BrandEntity();
        e.setId(id);
        e.setName(name);
        e.setLogoUrl("/img/logo.png");
        return e;
    }

    @Test
    void create_throws_ifNameExists() {
        when(repo.findByNameIgnoreCase("Apple"))
                .thenReturn(Optional.of(new BrandEntity()));

        CreateBrand req = new CreateBrand("Apple", "/logo");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void create_returnsSavedDto() {
        UUID id = UUID.randomUUID();
        when(repo.findByNameIgnoreCase("Apple")).thenReturn(Optional.empty());

        when(repo.save(any())).thenAnswer(inv -> {
            BrandEntity b = inv.getArgument(0);
            b.setId(id);
            return b;
        });

        BrandDto dto = service.create(new CreateBrand("Apple", "/logo"));

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Apple");
        assertThat(dto.logoUrl()).isEqualTo("/logo");
    }

    @Test
    void get_returnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(entity(id, "Apple")));

        BrandDto dto = service.get(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Apple");
    }

    @Test
    void get_throws_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void list_mapsEntitiesToDtos() {
        BrandEntity b1 = entity(UUID.randomUUID(), "Apple");
        BrandEntity b2 = entity(UUID.randomUUID(), "Umbrella");

        Page<BrandEntity> page = new PageImpl<>(List.of(b1, b2));
        Pageable pg = PageRequest.of(0, 10);

        when(repo.findAll(pg)).thenReturn(page);

        Page<BrandDto> result = service.list(pg);

        assertThat(result).hasSize(2)
                .extracting(BrandDto::name)
                .containsExactlyInAnyOrder("Apple", "Umbrella");
    }

    @Test
    void update_throws_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        BrandDto incoming = new BrandDto(id, "Apple", "/logo");

        assertThatThrownBy(() -> service.update(id, incoming))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_returnsModifiedDto() {
        UUID id = UUID.randomUUID();
        BrandEntity existing = entity(id, "Old");
        when(repo.findById(id)).thenReturn(Optional.of(existing));

        when(repo.save(existing)).then(inv -> inv.getArgument(0));

        BrandDto incoming = new BrandDto(id, "NewName", "/newlogo");

        BrandDto result = service.update(id, incoming);

        assertThat(result.name()).isEqualTo("NewName");
        assertThat(result.logoUrl()).isEqualTo("/newlogo");
    }

    @Test
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(repo).deleteById(id);
    }
}