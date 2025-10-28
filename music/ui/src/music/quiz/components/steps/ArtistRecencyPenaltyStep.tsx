import { type PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';

interface ArtistRecencyPenaltyStepProps {
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

export const ArtistRecencyPenaltyStep = (props: ArtistRecencyPenaltyStepProps) => {
  return (
    <BaseStep
      {...props}
      title="Artist Recency Penalty"
      description="Compensate artists that appeared in games recently"
    />
  );
};
