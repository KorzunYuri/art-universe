import { 
  type PipelineStepDto, 
  type FinalLimiterStepConfig, 
  parseStepConfig, 
  serializeStepConfig 
} from '@/music/quiz/types/pipeline-steps.ts';
import { StepPreview } from '../StepPreview/StepPreview.tsx';
import { StepStats } from '../StepStats/StepStats.tsx';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface FinalLimiterStepProps {
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

export const FinalLimiterStep = ({ 
  step, 
  onUpdate, 
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: FinalLimiterStepProps) => {
  const config = parseStepConfig(step.type, step.cfgData) as FinalLimiterStepConfig;
  const targetCount = config.targetCount || 10;

  const handleTargetCountChange = (newTargetCount: number) => {
    const newConfig: FinalLimiterStepConfig = {
      type: 'FINAL_LIMITER',
      targetCount: newTargetCount
    };
    
    onUpdate({
      ...step,
      cfgData: serializeStepConfig(newConfig)
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>Final Limiter</h4>
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
      </div>

      {step.id && <StepPreview stepId={step.id} previewData={step.previewData} />}
      <StepStats resultStats={step.resultStats} />
    </div>
  );
};
