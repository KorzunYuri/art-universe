import { useCallback } from 'react';
import { useSpotifyEntity } from '@/music/data/raw/spotify/hooks/useSpotifyEntity';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { EntityBinding } from '@/music/data/raw/shared/components/EntityBinding';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import type { SpotifySupportedEntityType } from '@/music/data/raw/spotify/types/spotify-entity';

interface SpotifyBindingCellProps {
    entityType: SpotifySupportedEntityType;
    entityId: number;
}

export const SpotifyBindingCell = ({ entityType, entityId }: SpotifyBindingCellProps) => {
    const permissions = usePermissions();
    const readOnly = permissions.spotifyCurationAccess !== 'full';
    const { invalidateEntity } = useSpotifyEntity(entityType, entityId);

    const linkToMasterUrl = useCallback(
        (masterId: number) => getMasterEntityUrl(entityType, masterId),
        [entityType]
    );

    return (
        <span style={{flex: 1}}
              onClick={(e) => e.stopPropagation()}>
            <EntityBinding
                dataSource="spotify"
                entityType={entityType}
                entityId={entityId}
                readOnly={readOnly}
                onAfterBind={invalidateEntity}
                onAfterUnbind={invalidateEntity}
                linkToMasterUrl={linkToMasterUrl}
            />
        </span>
    );
};
