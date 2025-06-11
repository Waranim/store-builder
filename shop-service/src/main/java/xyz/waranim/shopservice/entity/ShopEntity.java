package xyz.waranim.shopservice.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Type;
import xyz.waranim.common.BaseEntity;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shop", schema = "shop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopEntity extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, unique = true)
    private String slug;

    private String name;

    private String description;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> theme;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;
}
