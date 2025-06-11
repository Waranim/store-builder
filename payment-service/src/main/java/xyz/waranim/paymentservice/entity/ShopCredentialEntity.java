package xyz.waranim.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import xyz.waranim.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "shop_credentials")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ShopCredentialEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID storeId;

    private String sellerId;

    @Column(nullable = false)
    private String oauthToken;
}
