import {type ReactNode, useCallback, useState} from "react";
import type {Notification, NotificationType} from "@/shared/types/notification.ts";
import {NotificationContext} from "@/shared/contexts/NotificationContext.tsx";

interface NotificationProviderProps {
    children: ReactNode;
}

export function NotificationProvider({ children }: NotificationProviderProps) {
    const [notifications, setNotifications] = useState<Notification[]>([]);

    const showNotification = useCallback((type: NotificationType, message: string, duration = 5000) => {
        const id = Date.now().toString() + Math.random().toString(36).substr(2, 9);
        const notification: Notification = { id, type, message, duration };

        setNotifications(prev => [...prev, notification]);

        // Auto-remove after duration
        setTimeout(() => {
            setNotifications(prev => prev.filter(n => n.id !== id));
        }, duration);
    }, []);

    const removeNotification = useCallback((id: string) => {
        setNotifications(prev => prev.filter(n => n.id !== id));
    }, []);

    return (
        <NotificationContext.Provider value={{ notifications, showNotification, removeNotification }}>
            {children}
        </NotificationContext.Provider>
    );
}
