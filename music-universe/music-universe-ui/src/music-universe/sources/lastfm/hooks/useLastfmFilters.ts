import { useState } from 'react';
import { useAdditionalSearchFields } from '@/music-universe/shared/hooks/useAdditionalSearchFields';

/**
 * This file contains hooks used in search panels across Lastfm entity tables
 */

/**
 * Hook for managing play count filter
 * Used by artists, albums, and tracks
 */
export function usePlayCountFilter() {
    const [minPlayCount, setMinPlayCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minPlayCountField = createNumberField(
        'minPlayCount',
        'Min Play Count',
        minPlayCount,
        setMinPlayCount,
        { placeholder: 'e.g. 1000', min: 0 }
    );
    
    return {
        minPlayCount,
        setMinPlayCount,
        minPlayCountField
    };
}

/**
 * Hook for managing listeners count filter
 * Used by artists, albums, and tracks
 */
export function useListenersCountFilter() {
    const [minListenersCount, setMinListenersCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minListenersCountField = createNumberField(
        'minListenersCount',
        'Min Listeners Count',
        minListenersCount,
        setMinListenersCount,
        { placeholder: 'e.g. 500', min: 0 }
    );
    
    return {
        minListenersCount,
        setMinListenersCount,
        minListenersCountField
    };
}

/**
 * Hook for managing usage count filter
 * Used by tags
 */
export function useUsageCountFilter() {
    const [minUsageCount, setMinUsageCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minUsageCountField = createNumberField(
        'minUsageCount',
        'Min Usage Count',
        minUsageCount,
        setMinUsageCount,
        { placeholder: 'e.g. 10', min: 0 }
    );
    
    return {
        minUsageCount,
        setMinUsageCount,
        minUsageCountField
    };
}

/**
 * Hook for managing usage users count filter
 * Used by tags
 */
export function useUsageUsersCountFilter() {
    const [minUsageUsersCount, setMinUsageUsersCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minUsageUsersCountField = createNumberField(
        'minUsageUsersCount',
        'Min Users Count',
        minUsageUsersCount,
        setMinUsageUsersCount,
        { placeholder: 'e.g. 5', min: 0 }
    );
    
    return {
        minUsageUsersCount,
        setMinUsageUsersCount,
        minUsageUsersCountField
    };
}

/**
 * Hook for managing artist ID filter
 * Used by albums and tracks
 */
export function useArtistIdFilter() {
    const [artistId, setArtistId] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const artistIdField = createNumberField(
        'artistId',
        'Artist ID',
        artistId,
        setArtistId,
        { placeholder: 'Filter by artist ID', min: 1 }
    );
    
    return {
        artistId,
        setArtistId,
        artistIdField
    };
}

/**
 * Hook for managing tag ID filter
 * Used by artists, albums, and tracks
 */
export function useTagIdFilter() {
    const [tagId, setTagId] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const tagIdField = createNumberField(
        'tagId',
        'Tag ID',
        tagId,
        setTagId,
        { placeholder: 'Filter by tag ID', min: 1 }
    );
    
    return {
        tagId,
        setTagId,
        tagIdField
    };
}
