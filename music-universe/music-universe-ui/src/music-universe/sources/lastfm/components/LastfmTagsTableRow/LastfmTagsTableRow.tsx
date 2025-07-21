// hooks
import { useState, useEffect, memo } from "react";
// components
import { EntityBinding, ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import type { LastfmTag } from "@/music-universe/sources/lastfm/types/lastfm-tag";
import { updateApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import { bindCategoryToExisting, createAndBindCategory, unbindCategory, lookupCategories } from "@/music-universe/music-data/api/music-data-categories";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// types
import type { LookupEntity } from "@/music-universe/shared/types/lookup";
import type { MasterEntity } from "@/music-universe/shared/types/entities.ts";
import type { RawEntityTableRow } from "@/music-universe/shared/types/table-row";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import tagTableStyles from "../LastfmTagsTable/LastfmTagsTable.module.css";

interface LastfmTagTableRowProps extends RawEntityTableRow<LastfmTag> {
    preloadedLookupData?: LookupEntity[]
}

export const LastfmTagsTableRow = memo((
    {
        entity,
        preloadedLookupData = []
    }: LastfmTagTableRowProps) =>
{
    const [isApproving, setIsApproving] = useState(false);
    const [isBinding, setIsBinding] = useState(false);

    console.log('🔧 LastfmTagsTableRow RENDER for entity:', entity.id, entity.name, 'hasMasterEntity:', !!entity.masterEntity);

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 LastfmTagsTableRow MOUNTED for entity:', entity.id, entity.name);
        return () => {
            console.log('🔧 LastfmTagsTableRow UNMOUNTED for entity:', entity.id);
        };
    }, []); // Empty dependency array - only on mount/unmount

    // Status change handler
    const handleStatusChange = async (tagToUpdate: LastfmTag, newStatus: number) => {
        setIsApproving(true);
        try {
            await updateApprovalStatus(tagToUpdate, newStatus);
            // Note: No longer updating parent component directly
            // The entity will be updated in place via the class instance
        } catch (error) {
            console.error("Failed to update tag approval status:", error);
        } finally {
            setIsApproving(false);
        }
    };

    // Pre-bind handler
    const handleBeforeBind = async (tagToApprove: LastfmTag): Promise<boolean> => {
        if (tagToApprove.approvalStatus === ApprovalStatus.APPROVED) {
            return true;
        }

        // Approve the tag if not already approved
        try {
            setIsBinding(true);
            await updateApprovalStatus(tagToApprove, ApprovalStatus.APPROVED);
            return true;
        } catch (error) {
            console.error("Failed to approve tag:", error);
            return false;
        } finally {
            setIsBinding(false);
        }
    };

    // Bind to existing handler
    const handleBindToExisting = async (tagId: number, targetCategoryId: number): Promise<MasterEntity | null> => {
        setIsBinding(true);
        try {
            return await bindCategoryToExisting('LASTFM', tagId, targetCategoryId);
        } finally {
            setIsBinding(false);
        }
    };

    // Create and bind handler
    const handleCreateAndBind = async (tagId: number, name: string): Promise<MasterEntity | null> => {
        setIsBinding(true);
        try {
            return await createAndBindCategory('LASTFM', tagId, name);
        } finally {
            setIsBinding(false);
        }
    };

    // Unbind handler
    const handleUnbind = async (tagId: number): Promise<boolean> => {
        setIsBinding(true);
        try {
            return await unbindCategory('LASTFM', tagId);
        } finally {
            setIsBinding(false);
        }
    };

    // After bind handler
    const handleAfterBind = (updatedEntity: LastfmTag) => {
        // Entity is already updated in place via the class instance
        // No need to notify parent component
    };

    return (
        <div key={entity.id} className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${tagTableStyles.name}`}>
                {entity.url ? (
                    <ExternalLink href={entity.url} label={entity.name}/>
                ) : (
                    <span>{entity.name}</span>
                )}
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.status}`}>
                <ApprovalToggle
                    status={entity.approvalStatus}
                    onChange={(newStatus) => handleStatusChange(entity, newStatus)}
                    disabled={isApproving || isBinding}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.binding}`}>
                <EntityBinding
                    key={`exp-entity-binding-${entity.id}`}
                    entity={entity}
                    onBindToExisting={handleBindToExisting}
                    onCreateAndBind={handleCreateAndBind}
                    onUnbind={handleUnbind}
                    onBeforeBind={handleBeforeBind}
                    onAfterBind={handleAfterBind}
                    lookupFunction={lookupCategories}
                    preloadedOptions={preloadedLookupData}
                    disabled={isApproving || isBinding}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={entity.usageCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={entity.usageUsersCount}/>
            </div>
        </div>
    );
});
