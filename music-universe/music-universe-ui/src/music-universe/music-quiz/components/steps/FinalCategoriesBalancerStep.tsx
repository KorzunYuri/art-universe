import { MasterEntityPicker } from '@/music-universe/music-data/components/MasterEntityPicker/MasterEntityPicker';
import { CategoryWeightItem } from '../CategoryWeightItem/CategoryWeightItem';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type { CategoryWeight } from '../../types/generation-steps';
import type { StepComponentProps } from '../../types/step-registry';
import styles from '../StepBuilder/StepBuilder.module.scss';

export const FinalCategoriesBalancerStep = ({ step, onUpdate, onRemove, readonly = false }: StepComponentProps) => {
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

  const handleDefaultQuotaChange = (defaultQuota: number) => {
    onUpdate({
      ...step,
      defaultQuota
    });
  };

  const handleTargetCountChange = (targetCount: number) => {
    onUpdate({
      ...step,
      targetCount
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''}`}>
      <div className={styles.header}>
        <h4>Final Categories Balancer</h4>
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

        <div className={styles.targetCountSection}>
          <label>
            Default Quota:
            <input 
              type="range"
              min="0"
              max="0.9"
              step="0.01"
              value={step.defaultQuota || 0.5} 
              onChange={(e) => handleDefaultQuotaChange(Number(e.target.value))}
              disabled={readonly}
            />
            <span>{(step.defaultQuota || 0.5).toFixed(2)}</span>
          </label>
        </div>

        <div className={styles.targetCountSection}>
          <label>
            Target Count:
            <input 
              type="number" 
              value={step.targetCount || 10} 
              onChange={(e) => handleTargetCountChange(Number(e.target.value))}
              min="1"
              disabled={readonly}
            />
          </label>
        </div>
      </div>
    </div>
  );
};
