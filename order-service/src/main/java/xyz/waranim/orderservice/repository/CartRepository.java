package xyz.waranim.orderservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.orderservice.entity.CartEntity;

import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<CartEntity, UUID> {
}
