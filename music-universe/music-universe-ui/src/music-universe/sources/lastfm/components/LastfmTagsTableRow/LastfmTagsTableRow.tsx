// hooks
import { useState } from "react";
// components
import { ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import type { LastfmTag } from "@/music-universe/sources/lastfm/types/lastfm-tag";
import { updateTagApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-tags.ts";
// styles
import sharedTableStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import tagTableStyles from "../LastfmTagsTable/LastfmTagsTable.module.css";

interface LastfmTagTableRowProps {
    tag: LastfmTag,
    onChange: (tag: LastfmTag) => void
}

export const LastfmTagsTableRow = ({tag, onChange}: LastfmTagTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);

    function onStatusChange(tagToUpdate: LastfmTag, newStatus: number) {
        setIsApproving(true);
        updateTagApprovalStatus(tagToUpdate.id, newStatus)
            .then(updatedTag => {
                onChange(updatedTag);
            })
            .finally(() => {
                setIsApproving(false);
            });
    }

    return (
        <div key={tag.id} className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${tagTableStyles.name}`}>
                {tag.url ? (
                    <ExternalLink href={tag.url} label={tag.name}/>
                ) : (
                    <span>{tag.name}</span>
                )}
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.status}`}>
                <ApprovalToggle
                    status={tag.approvalStatus}
                    onChange={(newStatus) => onStatusChange(tag, newStatus)}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={tag.usageCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={tag.usageUsersCount}/>
            </div>
        </div>
    );
};
