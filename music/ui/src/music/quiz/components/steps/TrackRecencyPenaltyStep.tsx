import { type PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';

interface TrackRecencyPenaltyStepProps {
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

export const TrackRecencyPenaltyStep = (props: TrackRecencyPenaltyStepProps) => {
  return (
    <BaseStep
      {...props}
      title="Track Recency Penalty"
      description="Compensate tracks that appeared in games recently"
    />
  );
};
