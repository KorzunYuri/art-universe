// hooks
import { useState, useEffect, memo } from "react";
// components
import { ExternalLink, type LegacyEntityTableRow, ReadonlyAttr} from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import type { LastfmTag } from "@/music-universe/sources/lastfm/types/lastfm-tag";
import { updateApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
// constants
import {type ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus";
// types
import type { LookupEntity } from "@/music-universe/music-data/types/master-entities-lookup.ts";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import tagTableStyles from "../LastfmTagsTable/LastfmTagsTable.module.css";

interface LastfmTagTableRowProps extends LegacyEntityTableRow<LastfmTag> {
    preloadedLookupData?: LookupEntity[]
}

export const LastfmTagsTableRow = memo((
    {
        entity
    }: LastfmTagTableRowProps) =>
{
    const [isApproving, setIsApproving] = useState(false);

    console.log('🔧 LastfmTagsTableRow RENDER for entity:', entity.id, entity.name, 'hasMasterEntity:', !!entity.masterEntity);

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 LastfmTagsTableRow MOUNTED for entity:', entity.id, entity.name);
        return () => {
            console.log('🔧 LastfmTagsTableRow UNMOUNTED for entity:', entity.id);
        };
    }, []); // Empty dependency array - only on mount/unmount

    // Status change handler
    const handleStatusChange = async (tagToUpdate: LastfmTag, newStatus: ApprovalStatusType) => {
        setIsApproving(true);
        try {
            await updateApprovalStatus('category', tagToUpdate.id, newStatus);
            // Note: No longer updating parent component directly
            // The entity will be updated in place via the class instance
        } catch (error) {
            console.error("Failed to update tag approval status:", error);
        } finally {
            setIsApproving(false);
        }
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
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.binding}`}>
                {/*<EntityBinding*/}
                {/*    key={`exp-entity-binding-${entity.id}`}*/}
                {/*    entity={entity}*/}
                {/*    onBindToExisting={handleBindToExisting}*/}
                {/*    onCreateAndBind={handleCreateAndBind}*/}
                {/*    onUnbind={handleUnbind}*/}
                {/*    onBeforeBind={handleBeforeBind}*/}
                {/*    onAfterBind={handleAfterBind}*/}
                {/*    lookupFunction={(search) => lookupMasterEntities('category', search)}*/}
                {/*    preloadedOptions={preloadedLookupData}*/}
                {/*    disabled={isApproving || isBinding}*/}
                {/*/>*/}
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
