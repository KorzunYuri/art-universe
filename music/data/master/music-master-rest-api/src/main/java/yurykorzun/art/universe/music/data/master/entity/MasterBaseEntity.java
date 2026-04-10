package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatusConverter;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.model.OriginConverter;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public class MasterBaseEntity extends BaseEntity {

    @Builder.Default
    @Column(name = "origin", nullable = false, updatable = false)
    @Convert(converter = OriginConverter.class)
    private Origin origin = Origin.UNKNOWN;

    @Setter
    @Builder.Default
    @Column(name = "approval_status", nullable = false)
    @Convert(converter = MasterApprovalStatusConverter.class)
    private MasterApprovalStatus approvalStatus = MasterApprovalStatus.NEW;
}
