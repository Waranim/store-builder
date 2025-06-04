package xyz.waranim.catalogservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.catalogservice.dto.CategoryDto;
import xyz.waranim.catalogservice.dto.CreateCategory;
import xyz.waranim.catalogservice.entity.CategoryEntity;
import xyz.waranim.catalogservice.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryDto create(CreateCategory category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(category.name());
        if (category.parentId() != null) {
            entity.setParent(repo.findById(category.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Родительская категория не найдена")));
        }
        return CategoryDto.of(repo.save(entity));
    }

    public List<CategoryDto> tree() {
        return repo.findByParentIsNull()
                .stream().map(CategoryDto::of).toList();
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
