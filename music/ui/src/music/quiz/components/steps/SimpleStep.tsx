import { type PipelineStepDto, STEP_LABELS } from '@/music/quiz/types/pipeline-steps.ts';
import { StepPreview } from '../StepPreview/StepPreview.tsx';
import { StepStats } from '../StepStats/StepStats.tsx';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface SimpleStepProps {
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

export const SimpleStep = ({ 
  step, 
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: SimpleStepProps) => {
  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>{STEP_LABELS[step.type]}</h4>
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
        <p>No configuration required for this step type.</p>
      </div>

      {step.id && <StepPreview stepId={step.id} previewData={step.previewData} />}
      <StepStats resultStats={step.resultStats} />
    </div>
  );
};
