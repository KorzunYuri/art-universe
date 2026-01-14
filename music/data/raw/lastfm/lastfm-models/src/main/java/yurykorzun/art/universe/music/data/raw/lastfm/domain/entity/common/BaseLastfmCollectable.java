package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.domain.entity.Approvable;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatusConverter;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public class BaseLastfmCollectable extends BaseEntity implements Approvable {

    @Column(name = "approval_status")
    @Setter
    @Convert(converter = ApprovalStatusConverter.class)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Override
    public void updateApprovalStatus(ApprovalStatus approvalStatus) {
        if (approvalStatus == ApprovalStatus.PRE_APPROVED) {
            throw new IllegalArgumentException("Auto-approved status can be set only on creation");
        }
        this.approvalStatus = approvalStatus;
    }

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    protected LastfmApiCall apiCall;

}
