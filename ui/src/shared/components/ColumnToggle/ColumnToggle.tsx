import { useState, useRef, useEffect } from 'react';
import type { ColumnConfig } from '@/shared/hooks/useColumnPreferences';
import styles from './ColumnToggle.module.scss';

interface ColumnToggleProps {
    columns: Array<ColumnConfig & { visible: boolean }>;
    onToggle: (columnId: string) => void;
    onReset?: () => void;
}

export const ColumnToggle = ({ columns, onToggle, onReset }: ColumnToggleProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    // Close dropdown on outside click
    useEffect(() => {
        if (!isOpen) return;

        const handleClickOutside = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isOpen]);

    if (columns.length === 0) return null;

    return (
        <div className={styles.wrapper} ref={ref}>
            <button
                className={styles.trigger}
                onClick={() => setIsOpen(!isOpen)}
                type="button"
            >
                Columns
            </button>
            {isOpen && (
                <div className={styles.dropdown}>
                    {columns.map(col => (
                        <label key={col.id} className={styles.item}>
                            <input
                                type="checkbox"
                                checked={col.visible}
                                onChange={() => onToggle(col.id)}
                            />
                            <span>{col.label}</span>
                        </label>
                    ))}
                    {onReset && (
                        <button
                            className={styles.resetButton}
                            onClick={onReset}
                            type="button"
                        >
                            Reset to defaults
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};
