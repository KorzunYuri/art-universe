import type { MasterEntity } from '@/music-universe/shared/types/entity-reference';
import { MusicDataEntityType } from '@/music-universe/music-data/constants/entityTypes';

/**
 * Base class for all master entities with common method implementations
 */
abstract class BaseMasterEntity implements MasterEntity {
    constructor(public id: number, public name: string) {}
    
    getMasterEntity(): MasterEntity {
        return this;
    }
    
    hasMasterEntity(): true {
        return true;
    }
    
    abstract getEntityType(): string;
}

/**
 * Master Artist entity from music-data
 */
export interface Artist extends MasterEntity {
    getEntityType(): string;
}

/**
 * Master Album entity from music-data
 */
export interface Album extends MasterEntity {
    getEntityType(): string;
}

/**
 * Master Track entity from music-data
 */
export interface Track extends MasterEntity {
    primaryArtistId: number;
    getEntityType(): string;
}

/**
 * Master Category entity from music-data
 */
export interface Category extends MasterEntity {
    parentId?: number | null;
    parentName?: string | null;
    dimensionId?: number | null;
    dimensionName?: string | null;
    effectiveDimensionId?: number | null;
    effectiveDimensionName?: string | null;
    getEntityType(): string;
}

/**
 * Master Dimension entity from music-data
 */
export interface Dimension extends MasterEntity {
    getEntityType(): string;
}

/**
 * Implementation classes for master entities
 */
export class ArtistImpl extends BaseMasterEntity implements Artist {
    getEntityType(): string {
        return MusicDataEntityType.ARTIST;
    }
}

export class AlbumImpl extends BaseMasterEntity implements Album {
    getEntityType(): string {
        return MusicDataEntityType.ALBUM;
    }
}

export class TrackImpl extends BaseMasterEntity implements Track {
    constructor(
        id: number, 
        name: string, 
        public primaryArtistId: number
    ) {
        super(id, name);
    }
    
    getEntityType(): string {
        return MusicDataEntityType.TRACK;
    }
}

export class CategoryImpl extends BaseMasterEntity implements Category {
    constructor(
        id: number,
        name: string,
        public parentId?: number | null,
        public parentName?: string | null,
        public dimensionId?: number | null,
        public dimensionName?: string | null,
        public effectiveDimensionId?: number | null,
        public effectiveDimensionName?: string | null
    ) {
        super(id, name);
    }
    
    getEntityType(): string {
        return MusicDataEntityType.CATEGORY;
    }
}

export class DimensionImpl extends BaseMasterEntity implements Dimension {
    getEntityType(): string {
        return MusicDataEntityType.DIMENSION;
    }
}
