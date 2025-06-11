package xyz.waranim.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.waranim.catalogservice.entity.CategoryEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByParentIsNull();
    List<CategoryEntity> findByParentId(UUID parentId);

    @Query(value = """
        WITH RECURSIVE subtree AS (
             SELECT id FROM catalog.category WHERE id = :root
             UNION ALL
             SELECT c.id
             FROM   catalog.category c
             JOIN   subtree s ON c.parent_id = s.id
        )
        SELECT id FROM subtree
        """, nativeQuery = true)
    List<UUID> findSubtreeIds(@Param("root") UUID root);
}
