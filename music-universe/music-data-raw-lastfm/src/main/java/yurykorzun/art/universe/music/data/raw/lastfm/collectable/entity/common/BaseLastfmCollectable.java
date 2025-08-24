package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.entity.Approvable;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatusConverter;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public class BaseLastfmCollectable extends BaseEntity implements Approvable {

    @Column(name = "approval_status")
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
