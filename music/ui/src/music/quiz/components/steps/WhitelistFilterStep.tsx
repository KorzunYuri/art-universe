import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import type { LookupEntity } from '@/music/shared/types/lookup.ts';
import { 
  type PipelineStepDto, 
  type WhitelistFilterStepConfig, 
  parseStepConfig, 
  serializeStepConfig 
} from '@/music/quiz/types/pipeline-steps.ts';
import { StepPreview } from '../StepPreview/StepPreview.tsx';
import { StepStats } from '../StepStats/StepStats.tsx';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface WhitelistFilterStepProps {
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

export const WhitelistFilterStep = ({ 
  step, 
  onUpdate, 
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: WhitelistFilterStepProps) => {
  const config = parseStepConfig(step.type, step.cfgData) as WhitelistFilterStepConfig;
  const categories = config.categories || [];

  const handleCategoryAdd = (entity: LookupEntity) => {
    if (categories.some(cat => cat.id === entity.id)) return;
    
    const newConfig: WhitelistFilterStepConfig = {
      type: 'WHITELIST_FILTER',
      categories: [...categories, { id: entity.id, weight: 0.5 }]
    };
    
    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleWeightChange = (id: number, weight: number) => {
    const newConfig: WhitelistFilterStepConfig = {
      type: 'WHITELIST_FILTER',
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
    const newConfig: WhitelistFilterStepConfig = {
      type: 'WHITELIST_FILTER',
      categories: categories.filter(cat => cat.id !== id)
    };
    
    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>Whitelist Filter</h4>
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
