import {MasterEntityPicker} from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import type {LookupEntity} from '@/music/shared/types/lookup.ts';
import {
  type FinalCategoriesBalancerConfig,
  parseStepConfig,
  type PipelineStepDto,
  serializeStepConfig
} from '@/music/quiz/types/pipeline-steps.ts';
import {StepPreview} from '../StepPreview/StepPreview.tsx';
import {StepStats} from '../StepStats/StepStats.tsx';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface FinalCategoriesBalancerStepProps {
  step: PipelineStepDto;
  onUpdate: (step: PipelineStepDto) => void;
  onRemove?: () => void;
  onMoveUp?: () => void;
  onMoveDown?: () => void;
  onSave?: () => void;
  onExecute?: () => void;
  readonly?: boolean;
  isDirty?: boolean;
}

export const FinalCategoriesBalancerStep = ({ 
  step, 
  onUpdate, 
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: FinalCategoriesBalancerStepProps) => {
  const config = parseStepConfig(step.type, step.cfgData) as FinalCategoriesBalancerConfig;
  const categories = config.categories || [];
  const targetCount = config.targetCount || 10;
  const defaultQuota = config.defaultQuota || 0.5;

  const handleCategoryAdd = (entity: LookupEntity) => {
    if (categories.some(cat => cat.id === entity.id)) return;
    
    const newConfig: FinalCategoriesBalancerConfig = {
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
    const newConfig: FinalCategoriesBalancerConfig = {
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
    const newConfig: FinalCategoriesBalancerConfig = {
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
    const newConfig: FinalCategoriesBalancerConfig = {
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
    const newConfig: FinalCategoriesBalancerConfig = {
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
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>Final Categories Balancer</h4>
        <div className={styles.actions}>
          {!readonly && onMoveUp && (
            <button className={styles.moveButton} onClick={onMoveUp}>←</button>
          )}
          {!readonly && onMoveDown && (
            <button className={styles.moveButton} onClick={onMoveDown}>→</button>
          )}
          {!readonly && onSave && isDirty && (
            <button className={styles.saveButton} onClick={onSave}>Save</button>
          )}
          {!readonly && onExecute && step.id && (
            <button className={styles.executeButton} onClick={onExecute}>Execute</button>
          )}
          {!readonly && onRemove && (
            <button className={styles.removeButton} onClick={onRemove}>×</button>
          )}
        </div>
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
                <span className={styles.categoryName}>Category ID: {category.id}</span>
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

      {step.id && <StepPreview stepId={step.id} previewData={step.previewData} />}
      <StepStats resultStats={step.resultStats} />
    </div>
  );
};
