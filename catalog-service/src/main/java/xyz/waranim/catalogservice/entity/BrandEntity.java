package xyz.waranim.catalogservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.waranim.common.BaseEntity;

import java.util.List;

@Entity
@Table(name = "brand", schema = "catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<ProductEntity> products;
}
