/**
 * Constants for LastFM approval status values
 * These values correspond to the approval status on the LastFM backend
 */
export const ApprovalStatus = {
    PENDING: 1,
    APPROVED: 2,
    DECLINED: 3,
    AUTOAPPROVED: 4,
    IGNORED: 5
} as const;

export type ApprovalStatusType = typeof ApprovalStatus[keyof typeof ApprovalStatus];
