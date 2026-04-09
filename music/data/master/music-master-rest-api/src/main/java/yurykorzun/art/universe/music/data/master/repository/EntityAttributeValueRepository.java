package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.entity.attribute.EntityAttributeValue;

import java.util.List;

@Repository
public interface EntityAttributeValueRepository extends JpaRepository<EntityAttributeValue, Long> {

    @Query("""
        SELECT v FROM entity_attribute_value v
        JOIN FETCH v.attributeDef
        WHERE v.targetType = :targetType AND v.targetId = :targetId
        ORDER BY v.attributeDef.name, v.createdAt DESC
        """)
    List<EntityAttributeValue> findByTarget(
        @Param("targetType") AttributeTargetType targetType,
        @Param("targetId") Long targetId
    );

    @Query("""
        SELECT v FROM entity_attribute_value v
        JOIN FETCH v.attributeDef
        WHERE v.targetType = :targetType AND v.targetId = :targetId
          AND v.attributeDef.id = :attributeDefId
        ORDER BY v.createdAt DESC
        """)
    List<EntityAttributeValue> findByTargetAndDef(
        @Param("targetType") AttributeTargetType targetType,
        @Param("targetId") Long targetId,
        @Param("attributeDefId") Long attributeDefId
    );
}
