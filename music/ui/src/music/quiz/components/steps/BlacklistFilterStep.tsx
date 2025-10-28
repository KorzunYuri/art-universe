import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import { CategoryName } from '@/music/data/master/components/CategoryName/CategoryName.tsx';
import type { LookupEntity } from '@/music/shared/types/lookup.ts';
import { 
  type PipelineStepDto, 
  type BlacklistFilterStepConfig, 
  parseStepConfig, 
  serializeStepConfig 
} from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface BlacklistFilterStepProps {
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

export const BlacklistFilterStep = ({ 
  step, 
  onUpdate, 
  readonly = false,
  ...props
}: BlacklistFilterStepProps) => {
  const config = parseStepConfig(step.type, step.cfgData) as BlacklistFilterStepConfig;
  const categoryIds = config.categoryIds || [];

  const handleCategoryAdd = (entity: LookupEntity) => {
    if (categoryIds.includes(entity.id)) return;
    
    const newConfig: BlacklistFilterStepConfig = {
      type: 'BLACKLIST_FILTER',
      categoryIds: [...categoryIds, entity.id]
    };
    
    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  const handleCategoryRemove = (id: number) => {
    const newConfig: BlacklistFilterStepConfig = {
      type: 'BLACKLIST_FILTER',
      categoryIds: categoryIds.filter(catId => catId !== id)
    };
    
    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  return (
    <BaseStep
      step={step}
      title="Blacklist Filter"
      description="Filter out unwanted categories"
      readonly={readonly}
      {...props}
    >
      {!readonly && (
        <div className={styles.picker}>
          <MasterEntityPicker
            entityType="category"
            buttonLabel="Add"
            onEntitySelected={handleCategoryAdd}
          />
        </div>
      )}

      {categoryIds.length > 0 && (
        <div className={styles.categoriesList}>
          {categoryIds.map(id => (
            <div key={id} className={styles.categoryItem}>
              <span className={styles.categoryName}>
                <CategoryName categoryId={id} />
              </span>
              {!readonly && (
                <button
                  className={styles.removeButton}
                  onClick={() => handleCategoryRemove(id)}
                >
                  ×
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </BaseStep>
  );
};
