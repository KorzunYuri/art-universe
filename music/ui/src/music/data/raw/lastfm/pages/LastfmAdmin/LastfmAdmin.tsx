import { Navigate } from 'react-router-dom';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { MaintenanceButton } from '@/music/data/raw/lastfm/components/MaintenanceButton/MaintenanceButton';
import styles from './LastfmAdmin.module.css';

export function LastfmAdmin() {
    const permissions = usePermissions();

    if (!permissions.isAdmin()) {
        return <Navigate to="/music/data/raw/lastfm" replace />;
    }

    return (
        <div className={styles.container}>
            <h2>Last.fm Administration</h2>

            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Database</h3>
                <p className={styles.description}>
                    Trigger database maintenance tasks such as vacuum and reindex operations.
                </p>
                <MaintenanceButton />
            </section>
        </div>
    );
}
