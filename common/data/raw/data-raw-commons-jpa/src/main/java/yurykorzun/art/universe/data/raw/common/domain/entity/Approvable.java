package yurykorzun.art.universe.data.raw.common.domain.entity;

public interface Approvable {
    ApprovalStatus getApprovalStatus();
    void updateApprovalStatus(ApprovalStatus approvalStatus) throws IllegalArgumentException;
}
