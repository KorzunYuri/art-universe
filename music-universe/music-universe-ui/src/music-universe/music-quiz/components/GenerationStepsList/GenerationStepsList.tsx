import type { GenerationStepUI } from '../../types/generation-steps';
import styles from './GenerationStepsList.module.scss';

interface GenerationStepsListProps {
    steps: GenerationStepUI[];
    onStepRemove: (stepId: string) => void;
}

export const GenerationStepsList = ({ steps, onStepRemove }: GenerationStepsListProps) => {
    if (steps.length === 0) {
        return null;
    }

    return (
        <div className={styles.stepsList}>
            <h4>Generation Steps</h4>
            <div className={styles.steps}>
                {steps.map(step => (
                    <div key={step.id} className={styles.step}>
                        <div className={styles.stepInfo}>
                            <span className={styles.stepType}>{step.type}</span>
                            <span className={styles.categories}>
                                {step.categories.map(cat => cat.name).join(', ')}
                            </span>
                        </div>
                        <button
                            className={styles.removeButton}
                            onClick={() => onStepRemove(step.id)}
                            title="Remove step"
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};
