import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { ApprovalToggle } from '@/music/data/raw/lastfm/components/ApprovalToggle';
import type { LastfmSupportedEntityType } from '@/music/data/raw/lastfm/types/lastfm-entity';

interface LastfmApprovalCellProps {
    entityType: LastfmSupportedEntityType;
    entityId: number;
}

export const LastfmApprovalCell = ({ entityType, entityId }: LastfmApprovalCellProps) => {
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
            />
        </span>
    );
};
