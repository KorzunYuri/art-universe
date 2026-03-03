import type { MasterEntityType } from '@/music/shared/types/entities';

const ENTITY_TYPE_TO_URL: Record<MasterEntityType, ((id: number) => string) | null> = {
    artist: (id) => `/music/data/master/artists/${id}`,
    album: (id) => `/music/data/master/albums/${id}`,
    track: (id) => `/music/data/master/tracks/${id}`,
    category: null, // categories don't have individual detail pages
    person: (id) => `/art/data/master/persons/${id}`,
};

/**
 * Returns the URL path for a master entity's detail page.
 * Returns empty string for entity types without detail pages (e.g. category).
 */
export function getMasterEntityUrl(entityType: MasterEntityType, masterId: number): string {
    const urlFn = ENTITY_TYPE_TO_URL[entityType];
    if (!urlFn) return '';
    return urlFn(masterId);
}
