import { useState, useCallback } from 'react';
import type { SearchField } from '@/music-universe/shared/components/EntityTable/types';

/**
 * Hook for managing additional search fields state
 * Provides utilities for creating and managing search field configurations
 */
export function useAdditionalSearchFields() {
    
    /**
     * Creates a number search field
     */
    const createNumberField = useCallback((
        key: string,
        label: string,
        value: number | '',
        onChange: (value: number | '') => void,
        options?: {
            placeholder?: string;
            min?: number;
            max?: number;
            step?: number;
            disabled?: boolean;
        }
    ): SearchField => ({
        type: 'number',
        key,
        label,
        value,
        onChange,
        placeholder: options?.placeholder,
        min: options?.min,
        max: options?.max,
        step: options?.step,
        disabled: options?.disabled
    }), []);

    /**
     * Creates a text search field
     */
    const createTextField = useCallback((
        key: string,
        label: string,
        value: string,
        onChange: (value: string) => void,
        options?: {
            placeholder?: string;
            disabled?: boolean;
        }
    ): SearchField => ({
        type: 'text',
        key,
        label,
        value,
        onChange,
        placeholder: options?.placeholder,
        disabled: options?.disabled
    }), []);

    /**
     * Creates a select search field
     */
    const createSelectField = useCallback((
        key: string,
        label: string,
        value: string | number | '',
        onChange: (value: string | number | '') => void,
        options: Array<{ value: string | number; label: string }>,
        fieldOptions?: {
            placeholder?: string;
            disabled?: boolean;
        }
    ): SearchField => ({
        type: 'select',
        key,
        label,
        value,
        onChange,
        options,
        placeholder: fieldOptions?.placeholder,
        disabled: fieldOptions?.disabled
    }), []);

    /**
     * Creates a multi-select search field
     */
    const createMultiSelectField = useCallback((
        key: string,
        label: string,
        value: (string | number)[],
        onChange: (value: (string | number)[]) => void,
        options: Array<{ value: string | number; label: string }>,
        fieldOptions?: {
            disabled?: boolean;
        }
    ): SearchField => ({
        type: 'multiselect',
        key,
        label,
        value,
        onChange,
        options,
        disabled: fieldOptions?.disabled
    }), []);

    /**
     * Creates a custom search field
     */
    const createCustomField = useCallback((
        key: string,
        label: string,
        render: () => React.ReactNode,
        options?: {
            disabled?: boolean;
        }
    ): SearchField => ({
        type: 'custom',
        key,
        label,
        render,
        disabled: options?.disabled
    }), []);

    return {
        createNumberField,
        createTextField,
        createSelectField,
        createMultiSelectField,
        createCustomField
    };
}

/**
 * Hook for managing approval status filter
 * Common pattern across LastFM tables
 */
export function useApprovalStatusFilter() {
    const [approvalStatuses, setApprovalStatuses] = useState<number[]>([]);
    
    const { createMultiSelectField } = useAdditionalSearchFields();
    
    const approvalStatusOptions = [
        { value: 1, label: 'Pending' },
        { value: 2, label: 'Approved' },
        { value: 3, label: 'Declined' },
        { value: 4, label: 'Auto-approved' }
    ];
    
    const approvalStatusField = createMultiSelectField(
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

/**
 * Hook for managing count filters (play count, listeners count)
 * Common pattern across LastFM tables
 */
export function useCountFilters() {
    const [minPlayCount, setMinPlayCount] = useState<number | ''>('');
    const [minListenersCount, setMinListenersCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minPlayCountField = createNumberField(
        'minPlayCount',
        'Min Play Count',
        minPlayCount,
        setMinPlayCount,
        { placeholder: 'e.g. 1000', min: 0 }
    );
    
    const minListenersCountField = createNumberField(
        'minListenersCount',
        'Min Listeners Count',
        minListenersCount,
        setMinListenersCount,
        { placeholder: 'e.g. 500', min: 0 }
    );
    
    return {
        minPlayCount,
        setMinPlayCount,
        minListenersCount,
        setMinListenersCount,
        minPlayCountField,
        minListenersCountField
    };
}
