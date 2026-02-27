import { useCallback } from 'react';
import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { EntityBinding } from '@/music/data/raw/shared/components/EntityBinding';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import type { LastfmSupportedEntityType } from '@/music/data/raw/lastfm/types/lastfm-entity';

interface LastfmBindingCellProps {
    entityType: LastfmSupportedEntityType;
    entityId: number;
}

export const LastfmBindingCell = ({ entityType, entityId }: LastfmBindingCellProps) => {
    const { entity, updateEntity, invalidateEntity } = useLastfmEntity(entityType, entityId);

    const { ensureIsValidForBinding } = useLastfmEntityApproval(
        entity,
        'lastfm',
        updateEntity
    );

    const linkToMasterUrl = useCallback(
        (masterId: number) => getMasterEntityUrl(entityType, masterId),
        [entityType]
    );

    return (
        <span style={{flex:  1}}
              onClick={(e) => e.stopPropagation()}>
            <EntityBinding
                dataSource="lastfm"
                entityType={entityType}
                entityId={entityId}
                onBeforeBind={ensureIsValidForBinding}
                onAfterBind={invalidateEntity}
                onAfterUnbind={invalidateEntity}
                linkToMasterUrl={linkToMasterUrl}
            />
        </span>
    );
};
