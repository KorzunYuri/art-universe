import {
    BaseRawEntity,
    type MasterEntityMap,
    type MasterEntityType,
} from '@/music-universe/shared/types/entities.ts';
import type {Approvable} from '@/music-universe/shared/types/approvable';
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";

export type LastfmSupportedEntityType = Extract<
    MasterEntityType,
    'artist' | 'track' | 'category'
>;

export abstract class BaseLastfmEntity<T extends LastfmSupportedEntityType> extends BaseRawEntity<T> implements Approvable {
    approvalStatus: ApprovalStatusType;

    protected constructor(
        id: number,
        name: string,
        approvalStatus: ApprovalStatusType,
        masterEntity?: MasterEntityMap[T],
    ) {
        super(id, name, masterEntity);
        this.approvalStatus = approvalStatus;
    }

    getDataSource(): DataSource {
        return 'lastfm';
    }

    setApprovalStatus(approvalStatus: ApprovalStatusType): void {
        this.approvalStatus = approvalStatus;
    }
}

import {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import {LastfmTrack} from "@/music-universe/sources/lastfm/types/lastfm-track.ts";
import {LastfmTag} from "@/music-universe/sources/lastfm/types/lastfm-tag.ts";
export type LastfmSupportedEntityTypeMap = {
    artist:     LastfmArtist,
    track:      LastfmTrack,
    category:   LastfmTag,
}