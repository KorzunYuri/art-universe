package yurykorzun.art.universe.common.data.raw.entity;

public interface Approvable {
    ApprovalStatus getApprovalStatus();
    void updateApprovalStatus(ApprovalStatus approvalStatus) throws IllegalArgumentException;
}
