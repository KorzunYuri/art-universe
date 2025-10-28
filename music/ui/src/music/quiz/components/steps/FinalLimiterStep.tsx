import { 
  type PipelineStepDto, 
  type FinalLimiterStepConfig, 
  parseStepConfig, 
  serializeStepConfig 
} from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';
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
  readonly = false,
  ...props
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
    <BaseStep
      step={step}
      title="Final Limiter"
      description="Limit output to N tracks"
      readonly={readonly}
      {...props}
    >
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
    </BaseStep>
  );
};
