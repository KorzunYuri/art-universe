import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import { CategoryName } from '@/music/data/master/components/CategoryName/CategoryName.tsx';
import type { LookupEntity } from '@/shared/types/lookup.ts';
import {
  type PipelineStepDto,
  type FinalCategoriesBalancerConfig as FinalCategoriesBalancerStepConfig,
  parseStepConfig,
  serializeStepConfig
} from '@/music/quiz/types/pipeline-steps.ts';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface FinalCategoriesBalancerConfigProps {
  step: PipelineStepDto;
  onUpdate: (step: PipelineStepDto) => void;
  readonly?: boolean;
}

export const FinalCategoriesBalancerConfig = ({
  step,
  onUpdate,
  readonly = false
}: FinalCategoriesBalancerConfigProps) => {
  const config = parseStepConfig(step.type, step.cfgData) as FinalCategoriesBalancerStepConfig;
  const categories = config.categories || [];
  const targetCount = config.targetCount || 10;
  const defaultQuota = config.defaultQuota || 0.5;

  const handleCategoryAdd = (entity: LookupEntity) => {
    if (categories.some(cat => cat.id === entity.id)) return;

    const newConfig: FinalCategoriesBalancerStepConfig = {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount,
      defaultQuota,
      categories: [...categories, { id: entity.id, weight: 0.5 }]
    };

    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleWeightChange = (id: number, weight: number) => {
    const newConfig: FinalCategoriesBalancerStepConfig = {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount,
      defaultQuota,
      categories: categories.map(cat =>
        cat.id === id ? { ...cat, weight } : cat
      )
    };

    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleCategoryRemove = (id: number) => {
    const newConfig: FinalCategoriesBalancerStepConfig = {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount,
      defaultQuota,
      categories: categories.filter(cat => cat.id !== id)
    };

    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleTargetCountChange = (newTargetCount: number) => {
    const newConfig: FinalCategoriesBalancerStepConfig = {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount: newTargetCount,
      defaultQuota,
      categories
    };

    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleDefaultQuotaChange = (newDefaultQuota: number) => {
    const newConfig: FinalCategoriesBalancerStepConfig = {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount,
      defaultQuota: newDefaultQuota,
      categories
    };

    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  return (
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

      <div className={styles.targetCountSection}>
        <label>
          Target Count:
          <input
            type="number"
            value={targetCount}
            onChange={(e) => handleTargetCountChange(Number(e.target.value))}
            min="1"
            disabled={readonly}
          />
        </label>
      </div>

      <div className={styles.targetCountSection}>
        <label>
          Default Quota:
          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={defaultQuota}
            onChange={(e) => handleDefaultQuotaChange(Number(e.target.value))}
            disabled={readonly}
          />
          <span>{defaultQuota.toFixed(2)}</span>
        </label>
      </div>

      {categories.length > 0 && (
        <div className={styles.categoriesList}>
          {categories.map(category => (
            <div key={category.id} className={styles.categoryItem}>
              <span className={styles.categoryName}>
                <CategoryName categoryId={category.id} />
              </span>
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={category.weight}
                onChange={(e) => handleWeightChange(category.id, Number(e.target.value))}
                disabled={readonly}
              />
              <span>{category.weight.toFixed(2)}</span>
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
  );
};
