import {type ReactNode} from 'react';
import {type PipelineStepDto} from '@/music/quiz/types/pipeline-steps.ts';
import {StepPreview} from '../StepPreview/StepPreview.tsx';
import {StepStats} from '../StepStats/StepStats.tsx';
import styles from './BaseStep.module.scss';

interface BaseStepProps {
  step: PipelineStepDto;
  title: string;
  description?: string;
  children?: ReactNode;
  onRemove?: () => void;
  onMoveUp?: () => void;
  onMoveDown?: () => void;
  onSave?: () => void;
  onExecute?: () => void;
  readonly?: boolean;
  isDirty?: boolean;
}

export const BaseStep = ({ 
  step,
  title,
  description,
  children,
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: BaseStepProps) => {
  return (
    <div className={`${styles.step} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>{title}</h4>
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

      {description && (
        <div className={styles.description}>
          <span>{description}</span>
        </div>
      )}

      {children && (
        <div className={styles.configuration}>
          {children}
        </div>
      )}

      {step.id && <StepPreview stepId={step.id} previewData={step.previewData} />}
      {step.id && <StepStats step={step} />}
    </div>
  );
};
