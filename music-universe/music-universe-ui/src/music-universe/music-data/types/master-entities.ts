import type { MasterEntity } from '@/music-universe/shared/types/entities.ts';

export type MasterEntityType = "artist" | "album" | "track" | "category" | "dimension";

export type MasterEntityMap = {
    artist: Artist;
    album: Album;
    track: Track;
    category: Category;
    dimension: Dimension;
};


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
    
    abstract getEntityType(): MasterEntityType;
}

/**
 * Master Artist entity from music-data
 */
export interface Artist extends MasterEntity {
    getEntityType(): MasterEntityType;
}

/**
 * Master Album entity from music-data
 */
export interface Album extends MasterEntity {
    getEntityType(): MasterEntityType;
}

/**
 * Master Track entity from music-data
 */
export interface Track extends MasterEntity {
    primaryArtistId: number;
    getEntityType(): MasterEntityType;
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
    getEntityType(): MasterEntityType;
}

/**
 * Master Dimension entity from music-data
 */
export interface Dimension extends MasterEntity {
    getEntityType(): MasterEntityType;
}

/**
 * Implementation classes for master entities
 */
export class ArtistImpl extends BaseMasterEntity implements Artist {
    getEntityType(): MasterEntityType {
        return "artist";
    }
}

export class AlbumImpl extends BaseMasterEntity implements Album {
    getEntityType(): MasterEntityType {
        return "album";
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
    
    getEntityType(): MasterEntityType {
        return "track";
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
    
    getEntityType(): MasterEntityType {
        return "category";
    }
}

export class DimensionImpl extends BaseMasterEntity implements Dimension {
    getEntityType(): MasterEntityType {
        return "dimension";
    }
}
