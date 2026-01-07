import type { CategoryWeight } from '@/music/quiz/types/generation-steps.ts';
import styles from './CategoryWeightItem.module.scss';
import stepStyles from '../StepBuilder/StepBuilder.module.scss';

interface CategoryWeightItemProps {
    category: CategoryWeight;
    onWeightChange: (id: number, weight: number) => void;
    onRemove: (id: number) => void;
    readonly?: boolean;
}

export const CategoryWeightItem = ({ category, onWeightChange, onRemove, readonly = false }: CategoryWeightItemProps) => {
    const handleWeightChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const weight = parseFloat(e.target.value);
        onWeightChange(category.id, weight);
    };

    return (
        <div className={styles.item}>
            <span className={stepStyles.categoryName}>{category.name}</span>
            <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={category.weight}
                onChange={handleWeightChange}
                className={styles.slider}
                disabled={readonly}
            />
            <span className={styles.weight}>{category.weight.toFixed(2)}</span>
            {!readonly && (
                <button
                    className={styles.removeButton}
                    onClick={() => onRemove(category.id)}
                    title="Remove category"
                >
                    ×
                </button>
            )}
        </div>
    );
};
