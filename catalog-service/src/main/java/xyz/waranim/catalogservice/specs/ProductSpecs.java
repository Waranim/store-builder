package xyz.waranim.catalogservice.specs;

import org.springframework.data.jpa.domain.Specification;
import xyz.waranim.catalogservice.entity.ProductEntity;

import java.util.Collection;
import java.util.UUID;

public class ProductSpecs {

    public static Specification<ProductEntity> byShop(UUID shopId) {
        return (root, q, cb) -> cb.equal(root.get("shopId"), shopId);
    }

    public static Specification<ProductEntity> active(Boolean onlyActive) {
        return onlyActive == null ? null
                : (root, q, cb) -> cb.equal(root.get("isActive"), onlyActive);
    }

    public static Specification<ProductEntity> byBrand(UUID brandId) {
        return brandId == null ? null
                : (root, q, cb) -> cb.equal(root.join("brand").get("id"), brandId);
    }

    public static Specification<ProductEntity> inCategories(Collection<UUID> ids) {
        return (ids == null || ids.isEmpty()) ? null
                : (root, q, cb) -> root.join("category").get("id").in(ids);
    }
}
