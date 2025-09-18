import { MasterEntityPicker } from '@/music-universe/music-data/components/MasterEntityPicker/MasterEntityPicker';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type { StepComponentProps } from '../../types/step-registry';
import styles from '../StepBuilder/StepBuilder.module.scss';

export const BlacklistFilterStep = ({ step, onUpdate, onRemove, readonly = false }: StepComponentProps) => {
  const handleCategoryAdd = (entity: LookupEntity) => {
    if (!step.blacklistCategories || step.blacklistCategories.some(cat => cat.id === entity.id)) return;
    onUpdate({
      ...step,
      categoryIds: [...(step.categoryIds || []), entity.id],
      blacklistCategories: [...(step.blacklistCategories || []), { id: entity.id, name: entity.name }]
    });
  };

  const handleCategoryRemove = (id: number) => {
    onUpdate({
      ...step,
      categoryIds: (step.categoryIds || []).filter(catId => catId !== id),
      blacklistCategories: (step.blacklistCategories || []).filter(cat => cat.id !== id)
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''}`}>
      <div className={styles.header}>
        <h4>Blacklist Filter</h4>
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

        {step.blacklistCategories && step.blacklistCategories.length > 0 && (
          <div className={styles.categoriesList}>
            {step.blacklistCategories.map(category => (
              <div key={category.id} className={styles.categoryItem}>
                <span className={styles.categoryName}>{category.name}</span>
                {!readonly && (
                  <button
                    className={styles.removeButton}
                    onClick={() => handleCategoryRemove(category.id)}
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
