import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
export type MasterEntityType = "artist" | "album" | "track" | "category";
export interface BaseEntity {
    id: number;
    name: string;
}
export interface MasterEntityHolder<T extends MasterEntityType> {
    getMasterEntity(): MasterEntityMap[T] | undefined;
    hasMasterEntity(): boolean;
}
export interface MasterEntity<T extends MasterEntityType> extends BaseEntity, MasterEntityHolder<T> {
    getEntityType(): T;
    getMasterEntity(): MasterEntityMap[T];
    hasMasterEntity(): true;
}
export interface RawEntity<T extends MasterEntityType> extends BaseEntity, MasterEntityHolder<T> {
    masterEntity?: MasterEntityMap[T];
    getDataSource(): DataSource;
    getEntityType(): T;
    hasMasterEntity(): boolean;
    getMasterEntity(): MasterEntityMap[T] | undefined;
    setMasterEntity(masterEntity: MasterEntityMap[T] | undefined): void;
}
export interface ArtistRelatedRawEntity<T extends MasterEntityType> extends RawEntity<T> {
    getExternalArtistId(): number | undefined;
    getMasterArtistId(): number | undefined;
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
    abstract getDataSource(): DataSource;
    abstract getEntityType(): T;
}
/** ------------------ Master Entity Interfaces ------------------ **/
export interface Artist extends MasterEntity<"artist"> {}
export interface Album extends MasterEntity<"album"> {
    primaryArtistId: number;
}
export interface Track extends MasterEntity<"track"> {
    primaryArtistId: number;
}
export interface Category extends MasterEntity<"category"> {
    parentId?: number | null;
    parentName?: string | null;
}
/** ------------------ Master Entity Implementations ------------------ **/
export class ArtistImpl extends BaseMasterEntity<"artist"> implements Artist {
    getEntityType(): "artist" {
        return "artist";
    }
}
export class AlbumImpl extends BaseMasterEntity<"album"> implements Album {
    constructor(id: number, name: string, public primaryArtistId: number) {
        super(id, name);
        this.primaryArtistId = primaryArtistId;
    }
    getEntityType(): "album" {
        return "album";
    }
}
export class TrackImpl extends BaseMasterEntity<"track"> implements Track {
    constructor(id: number, name: string, public primaryArtistId: number) {
        super(id, name);
        this.primaryArtistId = primaryArtistId;
    }
    getEntityType(): "track" {
        return "track";
    }
}
export class CategoryImpl extends BaseMasterEntity<"category"> implements Category {
    constructor(
        id: number,
        name: string
    ) {
        super(id, name);
    }
    getEntityType(): "category" {
        return "category";
    }
}
/** ------------------ Master Entity Map ------------------ **/
export type MasterEntityMap = {
    artist:     Artist;
    album:      Album;
    track:      Track;
    category:   Category;
};