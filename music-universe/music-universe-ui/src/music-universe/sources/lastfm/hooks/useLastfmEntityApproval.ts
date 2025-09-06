import { useCallback, useState } from 'react';
import type { LastfmEntity } from '@/music-universe/sources/lastfm/types/lastfm-entity';
import type { DataSource } from '@/music-universe/sources/shared/types/data-sources';
import {ApprovalStatus, type ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import {updateRawEntityApprovalStatus} from "@/music-universe/sources/shared/api/approval.tsx";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useNotifications} from "@/music-universe/shared/hooks";

export const useLastfmEntityApproval = <M extends MasterEntityType, T extends LastfmEntity<M>>(
    entity: T | undefined,
    dataSource: DataSource,
    updateEntity: (entity: T) => void
) => {
    const [isApproving, setIsApproving] = useState(false);
    const { showNotification } = useNotifications();

    const setApprovalStatus = useCallback((newStatus: ApprovalStatusType) => {
        console.log(`new status ${newStatus} for entity ${entity?.id}`);
        if (!entity) return;
        setIsApproving(true);
        updateRawEntityApprovalStatus(dataSource, entity.getEntityType(), entity.id, newStatus)
            .then(() => {
                entity.setApprovalStatus(newStatus);
                updateEntity(entity);
            })
            .finally(() => {
                setIsApproving(false);
            });
    }, [entity, updateEntity, dataSource]);

    const ensureIsValidForBinding = useCallback(async (hasMasterExisted: boolean) => {
        if (!entity) return false;
        if (entity.approvalStatus === ApprovalStatus.APPROVED) return true;
        if (    entity.approvalStatus === ApprovalStatus.PENDING
            ||  entity.approvalStatus === ApprovalStatus.AUTOAPPROVED
        ) {
            // Only send API request, don't update entity here - full reload will happen later
            await updateRawEntityApprovalStatus(dataSource, entity.getEntityType(), entity.id, ApprovalStatus.APPROVED);
            return true;
        }

        showNotification('error', `Entity has invalid status for binding: ${entity.approvalStatus}`);
        return false;
    }, [entity, dataSource, showNotification]);

    return {
        isApproving,
        setApprovalStatus,
        ensureIsValidForBinding
    };
};
