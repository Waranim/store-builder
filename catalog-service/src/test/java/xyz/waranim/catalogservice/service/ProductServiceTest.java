package xyz.waranim.catalogservice.service;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import xyz.waranim.catalogservice.dto.CreateProduct;
import xyz.waranim.catalogservice.dto.ProductDto;
import xyz.waranim.catalogservice.entity.ProductEntity;
import xyz.waranim.catalogservice.repository.ProductRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_ShouldThrow_WhenSkuExists() {
        CreateProduct dto = new CreateProduct("shopId", "name", "desc", BigDecimal.valueOf(100), "SKU1", "url");
        when(productRepository.findBySku("SKU1")).thenReturn(Optional.of(new ProductEntity()));

        assertThatThrownBy(() -> productService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Артикул уже занят: SKU1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_ShouldSaveAndReturnDto() {
        UUID shopId = UUID.randomUUID();
        CreateProduct dto = new CreateProduct(shopId.toString(), "name", "desc", BigDecimal.valueOf(100), "SKU1", "url");
        when(productRepository.findBySku("SKU1")).thenReturn(Optional.empty());

        ProductEntity saved = new ProductEntity();
        saved.setId(UUID.randomUUID());
        saved.setShopId(shopId);
        saved.setName("name");
        saved.setPrice(BigDecimal.valueOf(100));
        saved.setSku("SKU1");
        when(productRepository.save(any())).thenReturn(saved);

        ProductDto result = productService.create(dto);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.sku()).isEqualTo("SKU1");
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        UUID id = UUID.randomUUID();
        ProductEntity ent = new ProductEntity();
        ent.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(ent));

        ProductDto dto = productService.getById(id);
        assertThat(dto.id()).isEqualTo(id);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listByShop_ShouldUseCorrectRepoMethod() {
        UUID shopId = UUID.randomUUID();
        Pageable pageReq = PageRequest.of(0, 10);
        ProductEntity e1 = new ProductEntity(), e2 = new ProductEntity();
        Page<ProductEntity> page = new PageImpl<>(List.of(e1, e2));

        when(productRepository.findByShopId(shopId, pageReq)).thenReturn(page);
        Page<ProductDto> result1 = productService.listByShop(shopId, null, pageReq);
        assertThat(result1.getTotalElements()).isEqualTo(2);

        when(productRepository.findByShopIdAndIsActive(shopId, true, pageReq)).thenReturn(page);
        Page<ProductDto> result2 = productService.listByShop(shopId, true, pageReq);
        assertThat(result2.getTotalElements()).isEqualTo(2);

        when(productRepository.findByShopIdAndIsActive(shopId, false, pageReq)).thenReturn(page);
        Page<ProductDto> result3 = productService.listByShop(shopId, false, pageReq);
        assertThat(result3.getTotalElements()).isEqualTo(2);
    }

    @Test
    void update_ShouldModifyAndReturnDto() {
        UUID id = UUID.randomUUID();
        ProductEntity existing = new ProductEntity();
        existing.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        ProductDto incoming = new ProductDto(
                id,
                null,
                "newName",
                "newDesc",
                BigDecimal.valueOf(200),
                "newSku",
                "newUrl",
                true);
        ProductEntity saved = new ProductEntity();
        saved.setId(id);
        saved.setName("newName");
        saved.setIsActive(true);
        when(productRepository.save(existing)).thenReturn(saved);

        ProductDto result = productService.update(id, incoming);
        assertThat(result.name()).isEqualTo("newName");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_ShouldCallRepo_WhenExists() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        productService.delete(id);
        verify(productRepository).deleteById(id);
    }
}