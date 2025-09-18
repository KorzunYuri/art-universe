import { useState } from 'react';
import { useAdditionalSearchFields } from '@/music-universe/shared/hooks/useAdditionalSearchFields';

/**
 * Hook for managing approval status filter
 * Common pattern across all raw entity sources (LastFM, etc.)
 */
export function useApprovalStatusFilter() {
    const [approvalStatuses, setApprovalStatuses] = useState<number[]>([]);
    
    const { createNumberMultiSelectField } = useAdditionalSearchFields();
    
    const approvalStatusOptions = [
        { value: 1, label: 'Pending' },
        { value: 2, label: 'Approved' },
        { value: 3, label: 'Declined' },
        { value: 4, label: 'Auto-approved' },
        { value: 5, label: 'Ignored' }
    ];
    
    const approvalStatusField = createNumberMultiSelectField(
        'approvalStatuses',
        'Approval Status',
        approvalStatuses,
        setApprovalStatuses,
        approvalStatusOptions
    );
    
    return {
        approvalStatuses,
        setApprovalStatuses,
        approvalStatusField
    };
}
