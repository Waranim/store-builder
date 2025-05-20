package xyz.waranim.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.catalogservice.entity.BrandEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, UUID> {
    Optional<BrandEntity> findByNameIgnoreCase(String name);
}
