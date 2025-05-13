package xyz.waranim.catalogservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.waranim.catalogservice.dto.CreateProduct;
import xyz.waranim.catalogservice.dto.ProductDto;
import xyz.waranim.catalogservice.entity.ProductEntity;
import xyz.waranim.catalogservice.repository.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductDto create(CreateProduct product) {
        if (productRepository.findBySku(product.sku()).isPresent()) {
            throw new IllegalArgumentException("Артикул уже занят: " + product.sku());
        }
        ProductEntity productEntity = new ProductEntity();
        productEntity.setShopId(UUID.fromString(product.shopId()));
        productEntity.setName(product.name());
        productEntity.setDescription(product.description());
        productEntity.setPrice(product.price());
        productEntity.setSku(product.sku());
        productEntity.setImageUrl(product.imageUrl());

        return ProductDto.of(productRepository.save(productEntity));
    }

    public ProductDto getById(UUID id) {
        return ProductDto.of(get(id));
    }

    private ProductEntity get(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + id));
    }

    public Page<ProductDto> listByShop(UUID shopId, Boolean onlyActive, Pageable pageable) {
        Page<ProductEntity> page;
        if (onlyActive != null) {
            page = productRepository.findByShopIdAndIsActive(shopId, onlyActive, pageable);
        } else {
            page = productRepository.findByShopId(shopId, pageable);
        }

        return page.map(ProductDto::of);
    }


    public ProductDto update(UUID id, ProductDto incoming) {
        ProductEntity existing = get(id);
        existing.setName(incoming.name());
        existing.setDescription(incoming.description());
        existing.setPrice(incoming.price());
        existing.setSku(incoming.sku());
        existing.setImageUrl(incoming.imageUrl());
        existing.setIsActive(incoming.isActive());
        return ProductDto.of(productRepository.save(existing));
    }


    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Товар не найден: " + id);
        }
        productRepository.deleteById(id);
    }
}
