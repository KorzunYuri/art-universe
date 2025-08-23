import { useState } from 'react';
import { triggerDbMaintenance } from '@/music-universe/sources/lastfm/api/lastfm-maintenance';
import { useNotifications } from '@/music-universe/shared/hooks/useNotifications';
import styles from './MaintenanceButton.module.scss';

export function MaintenanceButton() {
    const [isLoading, setIsLoading] = useState(false);
    const { showNotification } = useNotifications();

    const handleMaintenanceClick = async () => {
        setIsLoading(true);
        try {
            await triggerDbMaintenance();
            showNotification('success', 'Maintenance has been requested');
        } catch (error) {
            console.error('Failed to trigger maintenance:', error);
            showNotification('error', 'Failed to request maintenance');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <button
            onClick={handleMaintenanceClick}
            disabled={isLoading}
            className={styles.maintenanceButton}
        >
            {isLoading ? 'Requesting...' : 'DB Maintenance'}
        </button>
    );
}
