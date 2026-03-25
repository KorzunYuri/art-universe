import { Link, useParams } from 'react-router-dom';
import { useSpotifyEntity } from '@/music/data/raw/spotify/hooks/useSpotifyEntity';
import { ExternalLink } from '@/shared/components';
import { ArtistRelatedEntityBinding } from '@/music/data/raw/shared/components';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from '@/music/data/raw/spotify/styles/SpotifyDetailPage.module.scss';

export const SpotifyTrackDetail = () => {
    const { trackId } = useParams<{ trackId: string }>();
    const id = Number(trackId);

    const {
        entity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useSpotifyEntity('track', id);

    if (isLoading) {
        return <div className={styles.loading}>Loading track...</div>;
    }

    if (isError || !entity) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Track not found'}
            </div>
        );
    }

    const spotifyUrl = entity.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.track}${entity.spotifyId}`;

    return (
        <div className={styles.detail}>
            {/* Header */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                {entity.primaryArtistName && (
                    <span className={styles.artistName}>
                        by {entity.primaryArtistId
                            ? <Link to={`/music/data/raw/spotify/artists/${entity.primaryArtistId}`}>{entity.primaryArtistName}</Link>
                            : entity.primaryArtistName}
                    </span>
                )}
                <div className={styles.externalLinks}>
                    <ExternalLink href={spotifyUrl} label="Spotify" />
                </div>
            </div>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <ArtistRelatedEntityBinding
                        dataSource="spotify"
                        entityType="track"
                        entityId={id}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('track', masterId)}
                    />
                </div>
            </section>
        </div>
    );
};
