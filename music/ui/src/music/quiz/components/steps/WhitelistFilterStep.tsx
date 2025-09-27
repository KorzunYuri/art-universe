import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import { CategoryWeightItem } from '../CategoryWeightItem/CategoryWeightItem.tsx';
import type { LookupEntity } from '@/music/shared/types/lookup.ts';
import type { CategoryWeight } from '@/music/quiz/types/generation-steps.ts';
import type { StepComponentProps } from '@/music/quiz/types/step-registry.ts';
import styles from '../StepBuilder/StepBuilder.module.scss';

export const WhitelistFilterStep = ({ step, onUpdate, onRemove, readonly = false }: StepComponentProps) => {
  const handleCategoryAdd = (entity: LookupEntity) => {
    if (!step.categories || step.categories.some(cat => cat.id === entity.id)) return;
    const newCategory: CategoryWeight = {
      id: entity.id,
      name: entity.name,
      weight: 0.5
    };
    onUpdate({
      ...step,
      categories: [...(step.categories || []), newCategory]
    });
  };

  const handleWeightChange = (id: number, weight: number) => {
    onUpdate({
      ...step,
      categories: (step.categories || []).map(cat => 
        cat.id === id ? { ...cat, weight } : cat
      )
    });
  };

  const handleCategoryRemove = (id: number) => {
    onUpdate({
      ...step,
      categories: (step.categories || []).filter(cat => cat.id !== id)
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''}`}>
      <div className={styles.header}>
        <h4>Whitelist Filter</h4>
        {!readonly && onRemove && (
          <button className={styles.removeButton} onClick={onRemove}>×</button>
        )}
      </div>

      <div className={styles.content}>
        {!readonly && (
          <div className={styles.picker}>
            <MasterEntityPicker
              entityType="category"
              buttonLabel="Add"
              onEntitySelected={handleCategoryAdd}
            />
          </div>
        )}

        {step.categories && step.categories.length > 0 && (
          <div className={styles.categoriesList}>
            {step.categories.map(category => (
              <CategoryWeightItem
                key={category.id}
                category={category}
                onWeightChange={handleWeightChange}
                onRemove={handleCategoryRemove}
                readonly={readonly}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
