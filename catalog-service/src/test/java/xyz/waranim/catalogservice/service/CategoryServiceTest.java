package xyz.waranim.catalogservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.waranim.catalogservice.dto.CategoryDto;
import xyz.waranim.catalogservice.dto.CreateCategory;
import xyz.waranim.catalogservice.entity.CategoryEntity;
import xyz.waranim.catalogservice.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository repo;

    @InjectMocks
    CategoryService service;

    private CategoryEntity entity(UUID id, String name) {
        CategoryEntity e = new CategoryEntity();
        e.setId(id);
        e.setName(name);
        return e;
    }

    @Test
    void create_rootCategory_returnsDto() {
        UUID id = UUID.randomUUID();
        CreateCategory dto = new CreateCategory("Фрукты", null);

        when(repo.save(any())).thenAnswer(inv -> {
            CategoryEntity saved = inv.getArgument(0);
            saved.setId(id);
            return saved;
        });

        CategoryDto result = service.create(dto);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Фрукты");
        assertThat(result.children()).isEmpty();
    }

    @Test
    void create_withParent_returnsDto() {
        UUID parentId = UUID.randomUUID();
        UUID childId  = UUID.randomUUID();

        CategoryEntity parent = entity(parentId, "Продукты");
        when(repo.findById(parentId)).thenReturn(Optional.of(parent));

        when(repo.save(any())).thenAnswer(inv -> {
            CategoryEntity saved = inv.getArgument(0);
            saved.setId(childId);
            return saved;
        });

        CreateCategory dto = new CreateCategory("Фрукты", parentId);
        CategoryDto result = service.create(dto);

        assertThat(result.id()).isEqualTo(childId);
        verify(repo).findById(parentId);
    }

    @Test
    void create_throws_ifParentMissing() {
        UUID missingId = UUID.randomUUID();
        when(repo.findById(missingId)).thenReturn(Optional.empty());

        CreateCategory dto = new CreateCategory("Фрукты", missingId);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void tree_returnsRootList() {
        CategoryEntity root1 = entity(UUID.randomUUID(), "Продукты");
        CategoryEntity root2 = entity(UUID.randomUUID(), "Бытовая химия");

        when(repo.findByParentIsNull()).thenReturn(List.of(root1, root2));

        List<CategoryDto> tree = service.tree();

        assertThat(tree).hasSize(2)
                .extracting(CategoryDto::name)
                .containsExactlyInAnyOrder("Продукты", "Бытовая химия");
    }

    @Test
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(repo).deleteById(id);
    }
}