import {
    type Album, AlbumImpl,
    type Artist, ArtistImpl,
    type Track, TrackImpl,
    type Category, CategoryImpl,
    type MasterEntityType, type MasterEntityMap
} from "@/music-universe/music-data/types/master-entities.ts";

type BindingResponseMap = {
    artist:     BoundEntityResponse;
    album:      BoundEntityResponse;
    track:      TrackBoundEntityResponse;
    category:   BoundEntityResponse;
    dimension:  BoundEntityResponse;
};

export interface BoundEntityResponse {
    externalId: number;
    dataSource: string;
    masterId: number;
    masterName: string;
}

export interface TrackBoundEntityResponse extends BoundEntityResponse {
    primaryArtistId: number;
}

export function createMasterEntityFromBinding<K extends MasterEntityType>(
    res: BindingResponseMap[K],
    entityType: K
): MasterEntityMap[K] {
    switch (entityType) {
        case "artist":
            return createArtist(res) as MasterEntityMap[K];
        case "album":
            return createAlbum(res) as MasterEntityMap[K];
        case "track":
            return createTrack(res as TrackBoundEntityResponse) as MasterEntityMap[K];
        case "category":
            return createCategory(res) as MasterEntityMap[K];
        default:
            throw new Error("Unknown entity type or creation is not supported");
    }
}

export function createArtist(res: BoundEntityResponse): Artist {
    return new ArtistImpl(res.masterId, res.masterName);
}
export function createAlbum(res: BoundEntityResponse): Album {
    return new AlbumImpl(res.masterId, res.masterName);
}
export function createTrack(res: TrackBoundEntityResponse): Track {
    return new TrackImpl(res.masterId, res.masterName, res.primaryArtistId);
}
export function createCategory(res: BoundEntityResponse): Category {
    return new CategoryImpl(res.masterId, res.masterName);
}