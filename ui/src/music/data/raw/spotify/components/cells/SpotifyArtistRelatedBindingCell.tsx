import { useCallback } from 'react';
import { useSpotifyEntity } from '@/music/data/raw/spotify/hooks/useSpotifyEntity';
import { ArtistRelatedEntityBinding } from '@/music/data/raw/shared/components';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';

type ArtistRelatedEntityType = 'album' | 'track';

interface SpotifyArtistRelatedBindingCellProps {
    entityType: ArtistRelatedEntityType;
    entityId: number;
}

export const SpotifyArtistRelatedBindingCell = ({ entityType, entityId }: SpotifyArtistRelatedBindingCellProps) => {
    const { invalidateEntity } = useSpotifyEntity(entityType, entityId);

    const linkToMasterUrl = useCallback(
        (masterId: number) => getMasterEntityUrl(entityType, masterId),
        [entityType]
    );

    return (
        <span style={{flex: 1}}
              onClick={(e) => e.stopPropagation()}>
            <ArtistRelatedEntityBinding
                dataSource="spotify"
                entityType={entityType}
                entityId={entityId}
                onAfterBind={invalidateEntity}
                onAfterUnbind={invalidateEntity}
                linkToMasterUrl={linkToMasterUrl}
                compact
            />
        </span>
    );
};
