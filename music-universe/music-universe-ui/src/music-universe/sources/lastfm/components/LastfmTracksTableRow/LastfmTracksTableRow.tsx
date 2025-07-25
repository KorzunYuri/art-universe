// hooks
import { useState } from "react";
// components
import {
    ApprovalToggle,
    ExternalLink,
    type LegacyEntityTableRow,
    ReadonlyAttr
} from "@/music-universe/shared/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmTrack } from "@/music-universe/sources/lastfm/types/lastfm-track";
import { updateApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
// types
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import trackTableStyles from "../LastfmTracksTable/LastfmTracksTable.module.css";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

interface LastfmTrackTableRowProps extends LegacyEntityTableRow<LastfmTrack> {
}

export const LastfmTracksTableRow = ({entity}: LastfmTrackTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);

    function onStatusChange(trackToUpdate: LastfmTrack, newStatus: ApprovalStatusType) {
        setIsApproving(true);
        updateApprovalStatus('track', trackToUpdate.id, newStatus)
            .finally(() => {
                setIsApproving(false);
            });
    }

    return (
        <div key={entity.id} className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${trackTableStyles.artist}`}>
                {entity.artist && <ReadonlyAttr value={entity.artist.name} />}
            </div>
            
            <div className={`${sharedTableStyles.cell} ${trackTableStyles.name}`}>
                {entity.url && <ExternalLink href={entity.url} label={entity.name}/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.mbid}`}>
                {entity.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.track}${entity.mbid}`}
                        label="MusicBrainz"/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.status}`}>
                <ApprovalToggle
                    status={entity.approvalStatus}
                    onChange={(newStatus) => onStatusChange(entity, newStatus)}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.binding}`}>
                {/* EntityBinding temporarily removed */}
                <span>-</span>
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={entity.playCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={entity.listenersCount}/>
            </div>
        </div>
    );
};
