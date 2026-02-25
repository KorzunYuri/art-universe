import { type ReactNode, useState } from 'react';
import styles from './FoldableSection.module.scss';

interface FoldableSectionProps {
    title: string;
    defaultOpen?: boolean;
    children: ReactNode;
    className?: string;
}

export const FoldableSection = ({
    title,
    defaultOpen = false,
    children,
    className = '',
}: FoldableSectionProps) => {
    const [isOpen, setIsOpen] = useState(defaultOpen);

    return (
        <div className={`${styles.section} ${className}`}>
            <button
                className={styles.header}
                onClick={() => setIsOpen(!isOpen)}
                type="button"
            >
                <span className={`${styles.arrow} ${isOpen ? styles.open : ''}`}>
                    &#9654;
                </span>
                <span className={styles.title}>{title}</span>
            </button>
            {isOpen && (
                <div className={styles.content}>
                    {children}
                </div>
            )}
        </div>
    );
};
