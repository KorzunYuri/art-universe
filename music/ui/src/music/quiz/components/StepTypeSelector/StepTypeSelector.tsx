import { useState } from 'react';
import { STEP_LABELS, STEP_POSITIONS, type PipelineStepType, type StepPosition } from '@/music/quiz/types/pipeline-steps.ts';
import styles from './StepTypeSelector.module.scss';

interface StepTypeSelectorProps {
  allowedPositions: StepPosition[];
  onSelect: (stepType: PipelineStepType) => void | Promise<void>;
  onCancel: () => void;
}

export const StepTypeSelector = ({ allowedPositions, onSelect, onCancel }: StepTypeSelectorProps) => {
  const [selectedType, setSelectedType] = useState<PipelineStepType | ''>('');
  const [isLoading, setIsLoading] = useState(false);

  const availableSteps = Object.entries(STEP_POSITIONS)
    .filter(([, position]) => allowedPositions.includes(position))
    .map(([type]) => type as PipelineStepType);

  const handleSelect = async () => {
    if (selectedType) {
      setIsLoading(true);
      try {
        await onSelect(selectedType);
      } finally {
        setIsLoading(false);
      }
    }
  };

  return (
    <div className={styles.selector}>
      <div className={styles.header}>
        <h4>Select Step Type</h4>
        <button
          className={styles.cancelButton}
          onClick={onCancel}
          disabled={isLoading}
        >
          ×
        </button>
      </div>

      <div className={styles.content}>
        <select
          value={selectedType}
          onChange={(e) => setSelectedType(e.target.value as PipelineStepType)}
          className={styles.select}
          disabled={isLoading}
        >
          <option value="">Choose step type...</option>
          {availableSteps.map(stepType => (
            <option key={stepType} value={stepType}>
              {STEP_LABELS[stepType]}
            </option>
          ))}
        </select>

        <div className={styles.actions}>
          <button
            className={styles.selectButton}
            onClick={handleSelect}
            disabled={!selectedType || isLoading}
          >
            {isLoading ? 'Adding...' : 'Add Step'}
          </button>
        </div>
      </div>
    </div>
  );
};
