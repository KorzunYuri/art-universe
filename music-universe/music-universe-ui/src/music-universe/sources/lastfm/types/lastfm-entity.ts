import type {MasterEntityType, RawEntity} from '@/music-universe/shared/types/entities.ts';
import type {Approvable} from '@/music-universe/shared/types/approvable';
import {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import {LastfmTrack} from "@/music-universe/sources/lastfm/types/lastfm-track.ts";
import {LastfmTag} from "@/music-universe/sources/lastfm/types/lastfm-tag.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

export type LastfmSupportedEntityType = Extract<
    MasterEntityType,
    'artist' | 'track' | 'category'
>;

export type LastfmSupportedEntityTypeMap = {
    artist:     LastfmArtist,
    track:      LastfmTrack,
    category:   LastfmTag,
}

/**
 * Interface for LastFM entities that are both raw entities and approvable
 * @template M The type of master entity this LastFM entity can be bound to
 */
export interface LastfmEntity<T extends LastfmSupportedEntityType> extends RawEntity<T>, Approvable {
    approvalStatus: ApprovalStatusType;

    /**
     * Sets the approval status for this LastFM entity
     * @param approvalStatus The new approval status
     */
    setApprovalStatus(approvalStatus: ApprovalStatusType): void;
}

