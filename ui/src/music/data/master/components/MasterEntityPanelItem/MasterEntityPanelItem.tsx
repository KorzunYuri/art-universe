import styles from './MasterEntityPanelItem.module.scss';

export interface MasterEntityPanelItemProps {
    id: number;
    name: string;
    onRemove?: (id: number) => void;
    isProcessing?: boolean;
    removeTitle?: string;
}

export const MasterEntityPanelItem = ({
    id,
    name,
    onRemove,
    isProcessing = false,
    removeTitle = "Remove"
}: MasterEntityPanelItemProps) => {
    const displayName = name.length > 20 ? `${name.substring(0, 20)}...` : name;

    const handleRemove = () => {
        if (onRemove && !isProcessing) {
            onRemove(id);
        }
    };

    return (
        <div className={`${styles.panelItem} ${isProcessing ? styles.processing : ''}`}>
            <span className={styles.itemName} title={name}>
                {displayName}
            </span>
            {onRemove && (
                <button
                    className={styles.removeButton}
                    onClick={handleRemove}
                    disabled={isProcessing}
                    title={removeTitle}
                >
                    ×
                </button>
            )}
            {isProcessing && (
                <span className={styles.processingIndicator}>...</span>
            )}
        </div>
    );
};
