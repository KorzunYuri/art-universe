import type { MasterEntityType } from '@/music/shared/types/entities';

const ENTITY_TYPE_TO_PATH: Record<MasterEntityType, string | null> = {
    artist: 'artists',
    album: 'albums',
    track: 'tracks',
    category: null, // categories don't have individual detail pages
};

/**
 * Returns the URL path for a master entity's detail page.
 * Returns empty string for entity types without detail pages (e.g. category).
 */
export function getMasterEntityUrl(entityType: MasterEntityType, masterId: number): string {
    const path = ENTITY_TYPE_TO_PATH[entityType];
    if (!path) return '';
    return `/music/data/master/${path}/${masterId}`;
}
