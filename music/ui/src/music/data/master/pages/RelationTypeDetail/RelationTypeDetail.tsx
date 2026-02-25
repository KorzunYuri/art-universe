import { useParams } from 'react-router-dom';
import { useRelationTypeDetail } from '@/music/data/master/hooks/useRelationTypeDetail';
import styles from '../MasterDetailPage.module.scss';
import localStyles from './RelationTypeDetail.module.scss';

export const RelationTypeDetail = () => {
    const { relationTypeId } = useParams<{ relationTypeId: string }>();
    const id = Number(relationTypeId);

    const {
        relationType,
        isLoading,
        isError,
        error,
    } = useRelationTypeDetail(id);

    if (isLoading) {
        return <div className={styles.loading}>Loading...</div>;
    }

    if (isError || !relationType) {
        return (
            <div className={styles.error}>
                {error ? (error as any).message : 'Relation type not found'}
            </div>
        );
    }

    return (
        <div className={styles.detail}>
            {/* Header */}
            <div className={styles.header}>
                <span className={styles.entityName}>{relationType.name}</span>
                {relationType.isSystem && (
                    <span className={localStyles.systemBadge}>System</span>
                )}
            </div>

            {/* Reverse Name */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Reverse Name</h3>
                <div className={styles.attrValue}>
                    {relationType.reverseName ?? <span className={styles.stub}>—</span>}
                </div>
            </section>

            {/* Symmetrical */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Symmetrical</h3>
                <div className={styles.attrValue}>
                    {relationType.isSymmetrical ? 'Yes — relation goes both ways' : 'No — relation is directional'}
                </div>
            </section>

            {/* Applicabilities */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Applicabilities</h3>
                {relationType.applicabilities.length === 0 ? (
                    <div className={styles.stub}>No applicabilities defined</div>
                ) : (
                    <div className={localStyles.applicabilityList}>
                        {relationType.applicabilities.map((app) => (
                            <div key={app.id} className={localStyles.applicabilityRow}>
                                <span className={localStyles.applicabilityLabel}>
                                    {app.sourceEntityType} → {app.targetEntityType}
                                    {app.isDefault && (
                                        <span className={localStyles.defaultBadge}>default</span>
                                    )}
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
};
