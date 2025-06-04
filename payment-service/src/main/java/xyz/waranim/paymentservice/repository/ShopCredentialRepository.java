package xyz.waranim.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.paymentservice.entity.ShopCredentialEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopCredentialRepository extends JpaRepository<ShopCredentialEntity, UUID> {
    Optional<ShopCredentialEntity> findByStoreId(UUID storeId);
}
