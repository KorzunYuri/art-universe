export type MasterEntityType = "artist" | "album" | "track" | "category" | "dimension";

export interface BaseEntity {
    id: number;
    name: string;
}

export interface MasterEntityHolder<T extends MasterEntityType> {
    getMasterEntity(): MasterEntityMap[T] | undefined;
    hasMasterEntity(): boolean;
}

export interface MasterEntity<T extends MasterEntityType> extends BaseEntity, MasterEntityHolder<T> {
    getMasterEntity(): MasterEntityMap[T];
    hasMasterEntity(): true;
    getEntityType(): T;
}

export interface RawEntity<T extends MasterEntityType>
    extends BaseEntity,
        MasterEntityHolder<T> {
    masterEntity?: MasterEntityMap[T];
    getMasterEntity(): MasterEntityMap[T] | undefined;
    hasMasterEntity(): boolean;
    setMasterEntity(masterEntity: MasterEntityMap[T] | undefined): void;
    getEntityType(): T;
}

/** ------------------ Base Implementations ------------------ **/

export abstract class BaseMasterEntity<T extends MasterEntityType> implements MasterEntity<T> {
    constructor(public id: number, public name: string) {}

    getMasterEntity(): MasterEntityMap[T] {
        return this as unknown as MasterEntityMap[T];
    }

    hasMasterEntity(): true {
        return true;
    }

    abstract getEntityType(): T;
}

export abstract class BaseRawEntity<T extends MasterEntityType>
    implements RawEntity<T>
{
    constructor(public id: number, public name: string, public masterEntity?: MasterEntityMap[T]) {}

    getMasterEntity(): MasterEntityMap[T] | undefined {
        return this.masterEntity;
    }

    hasMasterEntity(): boolean {
        return this.masterEntity !== undefined;
    }

    setMasterEntity(masterEntity: MasterEntityMap[T] | undefined): void {
        this.masterEntity = masterEntity;
    }

    abstract getEntityType(): T;
}

/** ------------------ Master Entity Interfaces ------------------ **/

export interface Artist extends MasterEntity<"artist"> {}
export interface Album extends MasterEntity<"album"> {}
export interface Track extends MasterEntity<"track"> {
    primaryArtistId: number;
}
export interface Category extends MasterEntity<"category"> {
    parentId?: number | null;
    parentName?: string | null;
    dimensionId?: number | null;
    dimensionName?: string | null;
    effectiveDimensionId?: number | null;
    effectiveDimensionName?: string | null;
}
export interface Dimension extends MasterEntity<"dimension"> {}

/** ------------------ Master Entity Implementations ------------------ **/

export class ArtistImpl extends BaseMasterEntity<"artist"> implements Artist {
    getEntityType(): "artist" {
        return "artist";
    }
}

export class AlbumImpl extends BaseMasterEntity<"album"> implements Album {
    getEntityType(): "album" {
        return "album";
    }
}

export class TrackImpl extends BaseMasterEntity<"track"> implements Track {
    constructor(id: number, name: string, public primaryArtistId: number) {
        super(id, name);
    }

    getEntityType(): "track" {
        return "track";
    }
}

export class CategoryImpl extends BaseMasterEntity<"category"> implements Category {
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

    getEntityType(): "category" {
        return "category";
    }
}

export class DimensionImpl extends BaseMasterEntity<"dimension"> implements Dimension {
    getEntityType(): "dimension" {
        return "dimension";
    }
}

/** ------------------ Master Entity Map ------------------ **/

export type MasterEntityMap = {
    artist: Artist;
    album: Album;
    track: Track;
    category: Category;
    dimension: Dimension;
};