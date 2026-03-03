import { useState } from 'react';
import { useAdditionalSearchFields } from '@/shared/hooks/useAdditionalSearchFields.ts';
import { EntityLookup } from '@/shared/components';
import { LookupContextFactory } from '@/shared/types/lookup-context.ts';
import type { LookupEntity } from '@/shared/types/lookup.ts';

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
 * Hook for managing artist filter
 * Used by albums and tracks
 */
export function useArtistFilter() {
    const [selectedArtist, setSelectedArtist] = useState<LookupEntity | null>(null);
    const [artistSearchString, setArtistSearchString] = useState('');
    
    const { createCustomField } = useAdditionalSearchFields();
    
    const artistField = createCustomField(
        'artist',
        'Artist',
        () => (
            <div>
                <label style={{ 
                    display: 'block', 
                    marginBottom: '0.25rem', 
                    fontSize: '0.875rem', 
                    fontWeight: '500',
                    color: '#495057'
                }}>
                    Artist:
                </label>
                <EntityLookup
                    dataSource="lastfm"
                    entityType="artist"
                    searchString={artistSearchString}
                    context={LookupContextFactory.basic()}
                    onSelect={setSelectedArtist}
                    onChange={setArtistSearchString}
                    selectedEntity={selectedArtist}
                    placeholder="Search for artist..."
                    autoSelectExactMatch={true}
                    limit={10}
                />
            </div>
        )
    );
    
    return {
        artistId: selectedArtist?.id,
        selectedArtist,
        setSelectedArtist,
        artistSearchString,
        setArtistSearchString,
        artistField
    };
}

/**
 * Hook for managing tag filter
 * Used by artists, albums, and tracks
 */
export function useTagFilter() {
    const [selectedTag, setSelectedTag] = useState<LookupEntity | null>(null);
    const [tagSearchString, setTagSearchString] = useState('');
    
    const { createCustomField } = useAdditionalSearchFields();
    
    const tagField = createCustomField(
        'tag',
        'Tag',
        () => (
            <div>
                <label style={{ 
                    display: 'block', 
                    marginBottom: '0.25rem', 
                    fontSize: '0.875rem', 
                    fontWeight: '500',
                    color: '#495057'
                }}>
                    Tag:
                </label>
                <EntityLookup
                    dataSource="lastfm"
                    entityType="category"
                    searchString={tagSearchString}
                    context={LookupContextFactory.basic()}
                    onSelect={setSelectedTag}
                    onChange={setTagSearchString}
                    selectedEntity={selectedTag}
                    placeholder="Search for tag..."
                    autoSelectExactMatch={true}
                    limit={10}
                />
            </div>
        )
    );
    
    return {
        tagId: selectedTag?.id,
        selectedTag,
        setSelectedTag,
        tagSearchString,
        setTagSearchString,
        tagField
    };
}
