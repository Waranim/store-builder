package xyz.waranim.paymentservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.paymentservice.entity.ReceiptEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptEntity, UUID> {
    Page<ReceiptEntity> findAllByPayment_Shop_StoreId(UUID storeId, Pageable pageable);
    Optional<ReceiptEntity> findByYookassaReceiptId(String yookassaReceiptId);
}
