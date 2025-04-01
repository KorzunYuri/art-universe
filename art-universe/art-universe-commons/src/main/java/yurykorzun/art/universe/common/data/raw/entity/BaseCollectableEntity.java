package yurykorzun.art.universe.common.data.raw.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import jakarta.persistence.*;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseCollectableEntity extends BaseEntity {

    @Column(name = "approval_status")
    @Convert(converter = ApprovalStatusConverter.class)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Transient
    abstract public CollectableEntityType getType();

}
