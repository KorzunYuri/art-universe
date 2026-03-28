import { useParams } from 'react-router-dom';
import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { ExternalLink, AccessGate } from '@/shared/components';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { EntityTagPanel } from '@/music/data/raw/lastfm/components';
import { ApprovalToggle, ArtistRelatedEntityBinding } from '@/music/data/raw/shared/components';
import { QuizBinding } from '@/music/quiz/components';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from '@/music/data/raw/lastfm/styles/LastfmDetailPage.module.scss';

export const LastfmTrackDetail = () => {
    const { trackId } = useParams<{ trackId: string }>();
    const id = Number(trackId);

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useLastfmEntity('track', id);

    const {
        isApproving,
        setApprovalStatus,
        ensureIsValidForBinding,
    } = useLastfmEntityApproval(entity, 'lastfm', updateEntity);

    const permissions = usePermissions();
    const curationReadOnly = permissions.lastfmCurationAccess !== 'full';

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

    return (
        <div className={styles.detail}>
            {/* Header: name + external links */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                {entity.artist && (
                    <span className={styles.artistName}>by {entity.artist.name}</span>
                )}
                <div className={styles.externalLinks}>
                    {entity.url && <ExternalLink href={entity.url} label="Last.fm" />}
                    {entity.mbid && (
                        <ExternalLink
                            href={`${LastfmConfig.mbBaseUrls.track}${entity.mbid}`}
                            label="MusicBrainz"
                        />
                    )}
                </div>
            </div>

            {/* Attributes */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Stats</h3>
                <div className={styles.attrRow}>
                    <span className={styles.attrLabel}>Play count</span>
                    <span className={styles.attrValue}>{entity.playCount?.toLocaleString() ?? '—'}</span>
                </div>
                <div className={styles.attrRow}>
                    <span className={styles.attrLabel}>Listeners</span>
                    <span className={styles.attrValue}>{entity.listenersCount?.toLocaleString() ?? '—'}</span>
                </div>
            </section>

            {/* Approval */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Approval</h3>
                <div className={styles.controlRow}>
                    <ApprovalToggle
                        status={entity.approvalStatus}
                        onChange={setApprovalStatus}
                        disabled={isApproving}
                        readOnly={curationReadOnly}
                    />
                </div>
            </section>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <ArtistRelatedEntityBinding
                        dataSource="lastfm"
                        entityType="track"
                        entityId={id}
                        onBeforeBind={ensureIsValidForBinding}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('track', masterId)}
                        readOnly={curationReadOnly}
                    />
                </div>
            </section>

            {/* Quiz Binding */}
            <AccessGate level={permissions.quizAccess}>
                <section className={styles.section}>
                    <h3 className={styles.sectionTitle}>Quiz</h3>
                    <div className={styles.controlRow}>
                        <QuizBinding
                            entityType="track"
                            masterId={entity.getMasterEntity()?.id ?? null}
                        />
                    </div>
                </section>
            </AccessGate>

            {/* Tags */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Tags</h3>
                <EntityTagPanel
                    entityType="track"
                    entityId={entity.id}
                    entityApprovalStatus={entity.approvalStatus}
                    tagPageBaseUrl="/music/data/raw/lastfm/tags/"
                />
            </section>
        </div>
    );
};
