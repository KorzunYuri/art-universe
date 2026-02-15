import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { LastfmEntity } from '@/music/data/raw/lastfm/types/lastfm-entity.ts';
import type { DataSource } from '@/music/data/raw/shared/types/data-sources.ts';
import {ApprovalStatus, type ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import {updateRawEntityApprovalStatus} from "@/music/data/raw/shared/api/approval.tsx";
import type {MasterEntityType} from "@/music/shared/types/entities.ts";
import {useNotifications} from "@/music/shared/hooks";
import { rawEntitiesKeys } from "@/music/shared/utils/query-keys.ts";

export const useLastfmEntityApproval = <M extends MasterEntityType, T extends LastfmEntity<M>>(
    entity: T | undefined,
    dataSource: DataSource,
    updateEntity: (entity: T) => void
) => {
    const queryClient = useQueryClient();
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
                queryClient.invalidateQueries({ queryKey: rawEntitiesKeys.type(dataSource, entity.getEntityType()) });
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
