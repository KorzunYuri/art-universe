import { useCallback } from 'react';
import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { ArtistRelatedEntityBinding } from '@/music/data/raw/shared/components';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';

type ArtistRelatedEntityType = 'album' | 'track';

interface LastfmArtistRelatedBindingCellProps {
    entityType: ArtistRelatedEntityType;
    entityId: number;
}

export const LastfmArtistRelatedBindingCell = ({ entityType, entityId }: LastfmArtistRelatedBindingCellProps) => {
    const permissions = usePermissions();
    const readOnly = permissions.lastfmCurationAccess !== 'full';
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
            <ArtistRelatedEntityBinding
                dataSource="lastfm"
                entityType={entityType}
                entityId={entityId}
                readOnly={readOnly}
                onBeforeBind={ensureIsValidForBinding}
                onAfterBind={invalidateEntity}
                onAfterUnbind={invalidateEntity}
                linkToMasterUrl={linkToMasterUrl}
                compact
            />
        </span>
    );
};
