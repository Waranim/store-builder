package xyz.waranim.catalogservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.waranim.catalogservice.entity.ProductEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {
    Page<ProductEntity> findByShopIdAndIsActive(UUID shopId, Boolean isActive, Pageable pageable);
    Page<ProductEntity> findByShopId(UUID shopId, Pageable pageable);
    Optional<ProductEntity> findBySku(String sku);
}
