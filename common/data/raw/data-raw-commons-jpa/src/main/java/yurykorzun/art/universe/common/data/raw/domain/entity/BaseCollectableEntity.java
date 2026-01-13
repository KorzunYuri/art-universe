package yurykorzun.art.universe.common.data.raw.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

import jakarta.persistence.*;
import yurykorzun.art.universe.common.domain.entity.EntityType;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseCollectableEntity extends BaseEntity {

    @Column(name = "approval_status")
    @Convert(converter = ApprovalStatusConverter.class)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    public void updateApprovalStatus(ApprovalStatus approvalStatus) {
        if (approvalStatus == ApprovalStatus.PRE_APPROVED) {
            throw new IllegalArgumentException("Auto-approved status can be set only on creation");
        }
        this.approvalStatus = approvalStatus;
    }

    @Transient
    public abstract EntityType getEntityType();
}
