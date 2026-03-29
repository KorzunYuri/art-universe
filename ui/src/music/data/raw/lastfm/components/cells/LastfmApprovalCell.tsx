import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { ApprovalToggle } from '@/music/data/raw/shared/components';
import type { LastfmSupportedEntityType } from '@/music/data/raw/lastfm/types/lastfm-entity';

interface LastfmApprovalCellProps {
    entityType: LastfmSupportedEntityType;
    entityId: number;
}

export const LastfmApprovalCell = ({ entityType, entityId }: LastfmApprovalCellProps) => {
    const permissions = usePermissions();
    const readOnly = permissions.lastfmCurationAccess !== 'full';
    const { entity, updateEntity } = useLastfmEntity(entityType, entityId);

    const { isApproving, setApprovalStatus } = useLastfmEntityApproval(
        entity,
        'lastfm',
        updateEntity
    );

    if (!entity) return null;

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <ApprovalToggle
                status={entity.approvalStatus}
                onChange={setApprovalStatus}
                disabled={isApproving}
                readOnly={readOnly}
            />
        </span>
    );
};
