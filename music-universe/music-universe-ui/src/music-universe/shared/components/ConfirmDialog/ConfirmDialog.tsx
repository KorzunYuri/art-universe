import { useEffect } from 'react';
import styles from './ConfirmDialog.module.scss';

interface ConfirmDialogProps {
    isOpen: boolean;
    header: string;
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
}

export const ConfirmDialog = ({ isOpen, header, message, onConfirm, onCancel }: ConfirmDialogProps) => {
    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                onCancel();
            }
        };

        if (isOpen) {
            document.addEventListener('keydown', handleEscape);
            return () => document.removeEventListener('keydown', handleEscape);
        }
    }, [isOpen, onCancel]);

    if (!isOpen) return null;

    return (
        <div className={styles.overlay} onClick={onCancel}>
            <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
                <div className={styles.header}>
                    <h3>{header}</h3>
                </div>
                <div className={styles.content}>
                    <p>{message}</p>
                </div>
                <div className={styles.actions}>
                    <button className={styles.cancelButton} onClick={onCancel}>
                        No
                    </button>
                    <button className={styles.confirmButton} onClick={onConfirm}>
                        Yes
                    </button>
                </div>
            </div>
        </div>
    );
};
