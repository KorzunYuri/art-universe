import { useNotifications } from '@/shared/hooks/useNotifications';
import styles from './NotificationContainer.module.scss';

export function NotificationContainer() {
    const { notifications, removeNotification } = useNotifications();

    if (notifications.length === 0) {
        return null;
    }

    return (
        <div className={styles.container}>
            {notifications.map(notification => (
                <div
                    key={notification.id}
                    className={`${styles.notification} ${styles[notification.type]}`}
                    onClick={() => removeNotification(notification.id)}
                >
                    <span className={styles.message}>{notification.message}</span>
                    <button 
                        className={styles.closeButton}
                        onClick={(e) => {
                            e.stopPropagation();
                            removeNotification(notification.id);
                        }}
                    >
                        ×
                    </button>
                </div>
            ))}
        </div>
    );
}
