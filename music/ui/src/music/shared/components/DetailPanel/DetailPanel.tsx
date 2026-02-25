import type { ReactNode } from 'react';
import styles from './DetailPanel.module.scss';

interface DetailPanelProps {
    title?: string;
    onClose: () => void;
    children: ReactNode;
    className?: string;
}

export const DetailPanel = ({ title, onClose, children, className = '' }: DetailPanelProps) => {
    return (
        <div className={`${styles.panel} ${className}`}>
            <div className={styles.header}>
                {title && <h2 className={styles.title}>{title}</h2>}
                <button
                    onClick={onClose}
                    className={styles.closeButton}
                    aria-label="Close panel"
                >
                    &times;
                </button>
            </div>
            <div className={styles.content}>
                {children}
            </div>
        </div>
    );
};
