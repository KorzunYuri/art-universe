import { useContext } from 'react';
import { NotificationContext } from '@/music-universe/shared/contexts/NotificationContext';

export function useNotifications() {
    const context = useContext(NotificationContext);
    if (context === undefined) {
        throw new Error('useNotifications must be used within a NotificationProvider');
    }
    return context;
}
