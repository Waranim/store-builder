package xyz.waranim.shopservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.shopservice.entity.ShopEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, UUID> {
    Optional<ShopEntity> findBySlug(String slug);
}
