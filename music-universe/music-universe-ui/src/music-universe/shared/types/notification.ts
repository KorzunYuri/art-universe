export type NotificationType = 'success' | 'error';

export interface Notification {
    id: string;
    type: NotificationType;
    message: string;
    duration?: number; // in milliseconds, default 5000
}
