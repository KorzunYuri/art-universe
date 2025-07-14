// hooks
import { useState } from "react";
// components
import { EntityBinding, ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import type { LastfmTag } from "@/music-universe/sources/lastfm/types/lastfm-tag";
import { updateTagApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-tags.ts";
import { bindCategoryToExisting, createAndBindCategory, unbindCategory, lookupCategories } from "@/music-universe/music-data/api/music-data-categories";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// types
import type { LookupEntity } from "@/music-universe/shared/types/lookup";
// styles
import sharedTableStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import tagTableStyles from "../LastfmTagsTable/LastfmTagsTable.module.css";

interface LastfmTagTableRowProps {
    tag: LastfmTag,
    onChange: (tag: LastfmTag) => void,
    preloadedLookupData?: LookupEntity[]
}

export const LastfmTagsTableRow = ({tag, onChange, preloadedLookupData = []}: LastfmTagTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);

    function onStatusChange(tagToUpdate: LastfmTag, newStatus: number) {
        setIsApproving(true);
        updateTagApprovalStatus(tagToUpdate.id, newStatus)
            .then(updatedTag => {
                // Preserve the boundEntity information when updating approval status
                onChange({
                    ...updatedTag,
                    boundEntity: tag.boundEntity
                });
            })
            .finally(() => {
                setIsApproving(false);
            });
    }

    async function handleBeforeBind(tagToApprove: LastfmTag): Promise<boolean> {
        if (tagToApprove.approvalStatus === ApprovalStatus.APPROVED) {
            return true;
        }

        // Approve the tag if not already approved
        try {
            console.log("Tag not approved, approving first...");
            const approvedTag = await updateTagApprovalStatus(tagToApprove.id, ApprovalStatus.APPROVED);

            // Update the UI with approved tag
            onChange({
                ...approvedTag,
                boundEntity: tag.boundEntity
            });

            console.log("Tag approved successfully");
            return true;
        } catch (error) {
            console.error("Failed to approve tag:", error);
            return false;
        }
    }

    async function handleBindToExisting(tagId: number, targetCategoryId: number) {
        console.log("Binding to existing category...");
        return await bindCategoryToExisting('LASTFM', tagId, targetCategoryId);
    }

    async function handleCreateAndBind(tagId: number, name: string) {
        console.log("Creating and binding new category...");
        return await createAndBindCategory('LASTFM', tagId, name);
    }

    // Handle the result of binding from the EntityBinding component
    const handleAfterBind = (updatedEntity: LastfmTag) => {
        // If the entity was just bound (boundEntity changed from undefined to defined)
        if (updatedEntity.boundEntity && !tag.boundEntity) {
            console.log("Entity was bound, ensuring approval status is updated");
            // Ensure the approval status is set to APPROVED
            onChange({
                ...updatedEntity,
                approvalStatus: ApprovalStatus.APPROVED
            });
        } else {
            // For other changes, just pass through
            onChange(updatedEntity);
        }
    };

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

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.binding}`}>
                <EntityBinding
                    entity={tag}
                    onBindToExisting={handleBindToExisting}
                    onCreateAndBind={handleCreateAndBind}
                    onUnbind={(tagId) => unbindCategory('LASTFM', tagId)}
                    onBeforeBind={handleBeforeBind}
                    onAfterBind={handleAfterBind}
                    lookupFunction={lookupCategories}
                    preloadedOptions={preloadedLookupData}
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
