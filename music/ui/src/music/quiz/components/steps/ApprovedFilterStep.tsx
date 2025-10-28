import { type PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';

interface ApprovedFilterStepProps {
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

export const ApprovedFilterStep = (props: ApprovedFilterStepProps) => {
  return (
    <BaseStep
      {...props}
      title="Approved Filter"
      description="Filter our non-approved tracks/artists"
    />
  );
};
