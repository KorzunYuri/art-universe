import {
  type PipelineStepDto,
  type FinalLimiterStepConfig,
  parseStepConfig,
  serializeStepConfig
} from '@/music/quiz/types/pipeline-steps.ts';
import styles from '../StepBuilder/StepBuilder.module.scss';

interface FinalLimiterConfigProps {
  step: PipelineStepDto;
  onUpdate: (step: PipelineStepDto) => void;
  readonly?: boolean;
}

export const FinalLimiterConfig = ({
  step,
  onUpdate,
  readonly = false
}: FinalLimiterConfigProps) => {
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
  );
};
