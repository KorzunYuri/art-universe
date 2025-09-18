import type { StepComponentProps } from '../../types/step-registry';
import styles from '../StepBuilder/StepBuilder.module.scss';

export const FinalSelectionStep = ({ step, onUpdate, onRemove, readonly = false }: StepComponentProps) => {
  const handleTargetCountChange = (targetCount: number) => {
    onUpdate({
      ...step,
      targetCount
    });
  };

  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''}`}>
      <div className={styles.header}>
        <h4>Final Selection</h4>
        {!readonly && onRemove && (
          <button className={styles.removeButton} onClick={onRemove}>×</button>
        )}
      </div>

      <div className={styles.content}>
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
