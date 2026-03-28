import { useParams } from 'react-router-dom';
import { useSpotifyEntity } from '@/music/data/raw/spotify/hooks/useSpotifyEntity';
import { ExternalLink } from '@/shared/components';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { EntityBinding } from '@/music/data/raw/shared/components';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from '@/music/data/raw/spotify/styles/SpotifyDetailPage.module.scss';

export const SpotifyArtistDetail = () => {
    const { artistId } = useParams<{ artistId: string }>();
    const id = Number(artistId);

    const {
        entity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useSpotifyEntity('artist', id);

    const permissions = usePermissions();
    const curationReadOnly = permissions.spotifyCurationAccess !== 'full';

    if (isLoading) {
        return <div className={styles.loading}>Loading artist...</div>;
    }

    if (isError || !entity) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Artist not found'}
            </div>
        );
    }

    const spotifyUrl = entity.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.artist}${entity.spotifyId}`;

    return (
        <div className={styles.detail}>
            {/* Header */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                <div className={styles.externalLinks}>
                    <ExternalLink href={spotifyUrl} label="Spotify" />
                </div>
            </div>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <EntityBinding
                        dataSource="spotify"
                        entityType="artist"
                        entityId={id}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('artist', masterId)}
                        readOnly={curationReadOnly}
                    />
                </div>
            </section>
        </div>
    );
};
