import styles from './CategoryParentItem.module.scss';

export interface CategoryParentItemProps {
    id: number;
    name: string;
    onRemove?: (parentId: number) => void;
    isProcessing?: boolean;
}

export const CategoryParentItem = ({
    id,
    name,
    onRemove,
    isProcessing = false
}: CategoryParentItemProps) => {
    const displayName = name.length > 20 ? `${name.substring(0, 20)}...` : name;

    const handleRemove = () => {
        if (onRemove && !isProcessing) {
            onRemove(id);
        }
    };

    return (
        <div className={`${styles.parentItem} ${isProcessing ? styles.processing : ''}`}>
            <span className={styles.parentName} title={name}>
                {displayName}
            </span>
            {onRemove && (
                <button
                    className={styles.removeButton}
                    onClick={handleRemove}
                    disabled={isProcessing}
                    title="Remove parent"
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
