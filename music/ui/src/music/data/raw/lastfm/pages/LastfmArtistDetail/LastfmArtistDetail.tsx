import { useParams } from 'react-router-dom';
import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { useLastfmEntityApproval } from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import { ExternalLink } from '@/shared/components';
import { EntityTagPanel } from '@/music/data/raw/lastfm/components';
import { ApprovalToggle, EntityBinding } from '@/music/data/raw/shared/components';
import { QuizBinding } from '@/music/quiz/components';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from '@/music/data/raw/lastfm/styles/LastfmDetailPage.module.scss';

export const LastfmArtistDetail = () => {
    const { artistId } = useParams<{ artistId: string }>();
    const id = Number(artistId);

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useLastfmEntity('artist', id);

    const {
        isApproving,
        setApprovalStatus,
        ensureIsValidForBinding,
    } = useLastfmEntityApproval(entity, 'lastfm', updateEntity);

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

    return (
        <div className={styles.detail}>
            {/* Header: name + external links */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                <div className={styles.externalLinks}>
                    {entity.url && <ExternalLink href={entity.url} label="Last.fm" />}
                    {entity.mbid && (
                        <ExternalLink
                            href={`${LastfmConfig.mbBaseUrls.artist}${entity.mbid}`}
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
                    />
                </div>
            </section>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <EntityBinding
                        dataSource="lastfm"
                        entityType="artist"
                        entityId={id}
                        onBeforeBind={ensureIsValidForBinding}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('artist', masterId)}
                    />
                </div>
            </section>

            {/* Quiz Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Quiz</h3>
                <div className={styles.controlRow}>
                    <QuizBinding
                        entityType="artist"
                        masterId={entity.getMasterEntity()?.id ?? null}
                    />
                </div>
            </section>

            {/* Tags */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Tags</h3>
                <EntityTagPanel
                    entityType="artist"
                    entityId={entity.id}
                    entityApprovalStatus={entity.approvalStatus}
                    tagPageBaseUrl="/music/data/raw/lastfm/tags/"
                />
            </section>
        </div>
    );
};
