import { useParams } from 'react-router-dom';
import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { ExternalLink } from '@/music/shared/components';
import { ApprovalToggle, EntityBinding, EntityTagPanel } from '@/music/data/raw/lastfm/components';
import { QuizBinding } from '@/music/quiz/components';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from '../LastfmDetailPage.module.scss';

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
                <div className={styles.attrGrid}>
                    <span className={styles.attrLabel}>Play count</span>
                    <span className={styles.attrValue}>{entity.playCount?.toLocaleString() ?? '—'}</span>
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
                    />
                </div>
            </section>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <EntityBinding
                        dataSource="lastfm"
                        entityType="track"
                        entityId={id}
                        onBeforeBind={ensureIsValidForBinding}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('track', masterId)}
                    />
                </div>
            </section>

            {/* Quiz Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Quiz</h3>
                <div className={styles.controlRow}>
                    <QuizBinding
                        entityType="track"
                        masterId={entity.getMasterEntity()?.id ?? null}
                    />
                </div>
            </section>

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
