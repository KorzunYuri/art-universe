import { createContext } from 'react';
import type { Notification, NotificationType } from '@/music/shared/types/notification';

interface NotificationContextType {
    notifications: Notification[];
    showNotification: (type: NotificationType, message: string, duration?: number) => void;
    removeNotification: (id: string) => void;
}

export const NotificationContext = createContext<NotificationContextType | undefined>(undefined);
