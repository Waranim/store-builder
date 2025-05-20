package xyz.waranim.catalogservice.service;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import xyz.waranim.catalogservice.dto.CreateProduct;
import xyz.waranim.catalogservice.dto.ProductDto;
import xyz.waranim.catalogservice.entity.BrandEntity;
import xyz.waranim.catalogservice.entity.CategoryEntity;
import xyz.waranim.catalogservice.entity.ProductEntity;
import xyz.waranim.catalogservice.repository.BrandRepository;
import xyz.waranim.catalogservice.repository.CategoryRepository;
import xyz.waranim.catalogservice.repository.ProductRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository  productRepository;

    @Mock
    BrandRepository brandRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    ProductService service;

    private BrandEntity brand(UUID id) {
        BrandEntity b = new BrandEntity();
        b.setId(id);
        b.setName("Acme");
        return b;
    }

    private CategoryEntity category(UUID id) {
        CategoryEntity c = new CategoryEntity();
        c.setId(id);
        c.setName("Фрукты");
        return c;
    }

    private CreateProduct createDto(UUID shopId, UUID brandId, UUID catId) {
        return new CreateProduct(
                shopId.toString(),
                "Hat",
                "Desc",
                BigDecimal.valueOf(99),
                "SKU1",
                "url",
                brandId.toString(),
                catId.toString());
    }

    private ProductEntity product(UUID id, UUID shopId, BrandEntity b, CategoryEntity c) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setShopId(shopId);
        p.setName("Hat");
        p.setSku("SKU1");
        p.setBrand(b);
        p.setCategory(c);
        p.setPrice(BigDecimal.valueOf(99));
        p.setIsActive(true);
        return p;
    }

    @Test
    void create_throws_ifSkuExists() {
        CreateProduct dto = createDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(productRepository.findBySku("SKU1"))
                .thenReturn(Optional.of(new ProductEntity()));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_throws_ifBrandMissing() {
        UUID brandId = UUID.randomUUID();
        CreateProduct dto = createDto(UUID.randomUUID(), brandId, UUID.randomUUID());

        when(productRepository.findBySku("SKU1")).thenReturn(Optional.empty());
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_returnsSavedDto() {
        UUID shopId     = UUID.randomUUID();
        UUID brandId    = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        CreateProduct dto = createDto(shopId, brandId, categoryId);

        when(productRepository.findBySku("SKU1")).thenReturn(Optional.empty());
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand(brandId)));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category(categoryId)));

        ProductEntity saved = product(UUID.randomUUID(), shopId, brand(brandId), category(categoryId));
        when(productRepository.save(any())).thenReturn(saved);

        ProductDto result = service.create(dto);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.brandId()).isEqualTo(brandId);
        assertThat(result.categoryId()).isEqualTo(categoryId);
    }

    @Test
    void getById_returnsDto() {
        UUID id     = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID catId   = UUID.randomUUID();

        ProductEntity ent = product(id, shopId, brand(brandId), category(catId));
        when(productRepository.findById(id)).thenReturn(Optional.of(ent));

        ProductDto dto = service.getById(id);
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.brandId()).isEqualTo(brandId);
        assertThat(dto.categoryId()).isEqualTo(catId);
    }

    @Test
    void getById_throws_whenMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listByShop_callsCorrectRepoMethods() {
        UUID shopId = UUID.randomUUID();
        Pageable pg = PageRequest.of(0, 10);

        ProductEntity prod = product(UUID.randomUUID(), shopId,
                brand(UUID.randomUUID()), category(UUID.randomUUID()));
        Page<ProductEntity> page = new PageImpl<>(List.of(prod));

        when(productRepository.findByShopId(shopId, pg)).thenReturn(page);
        when(productRepository.findByShopIdAndIsActive(eq(shopId), anyBoolean(), eq(pg)))
                .thenReturn(page);

        assertThat(service.listByShop(shopId, null, pg)).hasSize(1);
        assertThat(service.listByShop(shopId, true, pg)).hasSize(1);
        assertThat(service.listByShop(shopId, false, pg)).hasSize(1);
    }

    @Test
    void search_buildsSpec_andDelegatesToRepo() {
        UUID shopId     = UUID.randomUUID();
        UUID brandId    = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findSubtreeIds(categoryId))
                .thenReturn(List.of(categoryId));

        ProductEntity prod = product(UUID.randomUUID(), shopId, brand(brandId), category(categoryId));
        Page<ProductEntity> page = new PageImpl<>(List.of(prod));

        when(productRepository.findAll(
                Mockito.<Specification<ProductEntity>>any(),
                any(Pageable.class))).thenReturn(page);

        Page<ProductDto> result = service.search(
                shopId, brandId, categoryId, true, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(
                ArgumentMatchers.<Specification<ProductEntity>>any(),
                any(Pageable.class));
    }

    @Test
    void update_throws_ifBrandMissing() {
        UUID id       = UUID.randomUUID();
        UUID brandId  = UUID.randomUUID();
        UUID catId    = UUID.randomUUID();

        ProductEntity existing = product(id, UUID.randomUUID(), brand(UUID.randomUUID()), category(catId));
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        ProductDto incoming = new ProductDto(id, existing.getShopId(), "Hat",
                brandId, catId,
                "Desc", BigDecimal.valueOf(99),
                "SKU1", "url", true);

        assertThatThrownBy(() -> service.update(id, incoming))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_returnsModifiedDto() {
        UUID id       = UUID.randomUUID();
        UUID shopId   = UUID.randomUUID();
        UUID brandId  = UUID.randomUUID();
        UUID catId    = UUID.randomUUID();

        ProductEntity existing = product(id, shopId, brand(UUID.randomUUID()), category(catId));
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand(brandId)));

        ProductEntity saved = product(id, shopId, brand(brandId), category(catId));
        saved.setName("New");
        when(productRepository.save(existing)).thenReturn(saved);

        ProductDto incoming = new ProductDto(id, shopId, "New",
                brandId, catId,
                "Desc", BigDecimal.valueOf(99),
                "SKU1", "url", true);

        ProductDto result = service.update(id, incoming);

        assertThat(result.name()).isEqualTo("New");
        assertThat(result.brandId()).isEqualTo(brandId);
        assertThat(result.categoryId()).isEqualTo(catId);
    }

    @Test
    void delete_throws_ifMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_correct() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(productRepository).deleteById(id);
    }
}