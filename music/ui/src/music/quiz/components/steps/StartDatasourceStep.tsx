import { type PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import { BaseStep } from './BaseStep.tsx';

interface StartDatasourceStepProps {
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

export const StartDatasourceStep = (props: StartDatasourceStepProps) => {
  return (
    <BaseStep
      {...props}
      title="Start Datasource"
      description="Choose start datasource"
    />
  );
};
