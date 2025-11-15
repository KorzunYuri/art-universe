import { useState, useEffect } from 'react';
import { useNotifications } from '@/music/shared/hooks/useNotifications.ts';
import {
  useAddStep,
  useMoveStep,
  useRemoveStep,
  useUpdateStepConfiguration,
  useExecuteStep
} from '@/music/quiz/hooks/useQuizData.ts';
import {
  type PipelineDto,
  type PipelineStepDto,
  type PipelineStepType,
  type StepPosition,
  validatePipeline,
  serializeStepConfig
} from '@/music/quiz/types/pipeline-steps.ts';
import { StepTypeSelector } from '../StepTypeSelector/StepTypeSelector.tsx';
import { PipelineStepper } from '../PipelineStepper/PipelineStepper.tsx';
import { PipelineStepDetail } from '../PipelineStepDetail/PipelineStepDetail.tsx';
import styles from './PipelineEditor.module.scss';

interface PipelineEditorProps {
  pipeline: PipelineDto;
  onPipelineUpdate: (pipeline: PipelineDto) => void;
}

export const PipelineEditor = ({ pipeline, onPipelineUpdate }: PipelineEditorProps) => {
  const [localSteps, setLocalSteps] = useState<PipelineStepDto[]>(pipeline.steps);
  const [selectedStepIndex, setSelectedStepIndex] = useState<number>(0);
  const [addingStepAt, setAddingStepAt] = useState<number | null>(null);
  const [dirtySteps, setDirtySteps] = useState<Set<number>>(new Set());

  const { showNotification } = useNotifications();
  const addStepMutation = useAddStep();
  const moveStepMutation = useMoveStep();
  const removeStepMutation = useRemoveStep();
  const updateStepMutation = useUpdateStepConfiguration();
  const executeStepMutation = useExecuteStep();

  const sortedSteps = [...localSteps].sort((a, b) => a.ord - b.ord);
  const validation = validatePipeline(sortedSteps);

  // Ensure selected step index is valid when steps change
  useEffect(() => {
    if (sortedSteps.length > 0 && selectedStepIndex >= sortedSteps.length) {
      setSelectedStepIndex(sortedSteps.length - 1);
    } else if (sortedSteps.length === 0) {
      setSelectedStepIndex(0);
    }
  }, [sortedSteps.length, selectedStepIndex]);

  const handleAddStepClick = (position: number) => {
    setAddingStepAt(position);
  };

  const handleStepTypeSelect = (stepType: PipelineStepType) => {
    if (addingStepAt === null) return;

    const newStep: PipelineStepDto = {
      type: stepType,
      ord: addingStepAt,
      cfgData: serializeStepConfig({ type: stepType } as any)
    };

    // Shift existing steps
    const updatedSteps = localSteps.map(step => 
      step.ord >= addingStepAt! ? { ...step, ord: step.ord + 1 } : step
    );
    
    setLocalSteps([...updatedSteps, newStep]);
    setAddingStepAt(null);
  };

  const handleStepUpdate = (step: PipelineStepDto, updatedStep: PipelineStepDto) => {
    const newSteps = localSteps.map(s => 
      s === step ? updatedStep : s
    );
    setLocalSteps(newSteps);
    
    // Mark as dirty if it has an ID (existing step)
    if (updatedStep.id) {
      setDirtySteps(prev => new Set(prev).add(updatedStep.id!));
    }
  };

  const handleStepSave = async (step: PipelineStepDto) => {
    try {
      if (step.id) {
        // Update existing step
        const updatedPipeline = await updateStepMutation.mutateAsync({
          pipelineId: pipeline.id,
          stepId: step.id,
          stepDto: step
        });
        onPipelineUpdate(updatedPipeline);
        setDirtySteps(prev => {
          const newSet = new Set(prev);
          newSet.delete(step.id!);
          return newSet;
        });
      } else {
        // Add new step
        const updatedPipeline = await addStepMutation.mutateAsync({
          pipelineId: pipeline.id,
          stepDto: step,
          position: step.ord
        });
        onPipelineUpdate(updatedPipeline);
        setLocalSteps(updatedPipeline.steps);
      }
      showNotification('success', 'Step saved successfully');
    } catch {
      showNotification('error', 'Failed to save step');
    }
  };

  const handleStepRemove = async (step: PipelineStepDto) => {
    try {
      if (step.id) {
        // Remove from backend
        const updatedPipeline = await removeStepMutation.mutateAsync({
          pipelineId: pipeline.id,
          stepId: step.id
        });
        onPipelineUpdate(updatedPipeline);
        setLocalSteps(updatedPipeline.steps);
      } else {
        // Remove locally
        setLocalSteps(prev => prev.filter(s => s !== step));
      }
      showNotification('success', 'Step removed successfully');
    } catch {
      showNotification('error', 'Failed to remove step');
    }
  };

  const handleReorder = async (fromIndex: number, toIndex: number) => {
    const step = sortedSteps[fromIndex];
    if (!step.id) return;

    try {
      const updatedPipeline = await moveStepMutation.mutateAsync({
        pipelineId: pipeline.id,
        stepId: step.id,
        newPosition: toIndex
      });
      onPipelineUpdate(updatedPipeline);
      setLocalSteps(updatedPipeline.steps);
      // Update selected index to follow the moved step
      setSelectedStepIndex(toIndex);
      showNotification('success', 'Step moved successfully');
    } catch {
      showNotification('error', 'Failed to move step');
    }
  };

  const handleStepExecute = async (step: PipelineStepDto) => {
    if (!step.id) return;

    try {
      const updatedPipeline = await executeStepMutation.mutateAsync({
        pipelineId: pipeline.id,
        stepId: step.id
      });
      onPipelineUpdate(updatedPipeline);
      setLocalSteps(updatedPipeline.steps);
      showNotification('success', 'Step executed successfully');
    } catch {
      showNotification('error', 'Failed to execute step');
    }
  };


  const getAvailablePositions = (position: number): StepPosition[] => {
    // Position 0: only INITIAL steps allowed
    // Position > 0: only TRANSFORM steps allowed
    if (position === 0) {
      return ['INITIAL'];
    }

    return ['TRANSFORM'];
  };

  const selectedStep = sortedSteps[selectedStepIndex];
  const readonly = pipeline.immutable;

  return (
    <div className={styles.editor}>
      {/* Validation messages */}
      <div className={styles.validation}>
        {!validation.isValid && (
          <div className={styles.validationErrors}>
            {validation.errors.map((error, index) => (
              <div key={index} className={styles.validationError}>{error}</div>
            ))}
          </div>
        )}
        {validation.warnings && validation.warnings.length > 0 && (
          <div className={styles.validationWarnings}>
            {validation.warnings.map((warning, index) => (
              <div key={index} className={styles.validationWarning}>{warning}</div>
            ))}
          </div>
        )}
      </div>

      {/* Pipeline Stepper - shows all steps */}
      {sortedSteps.length > 0 && (
        <>
          <PipelineStepper
            steps={sortedSteps}
            selectedStepIndex={selectedStepIndex}
            onStepSelect={setSelectedStepIndex}
            onReorder={handleReorder}
            onAddStep={handleAddStepClick}
            onRemoveStep={(index) => handleStepRemove(sortedSteps[index])}
            addingStepAt={addingStepAt}
            stepSelectorRenderer={(position) => (
              <StepTypeSelector
                allowedPositions={getAvailablePositions(position)}
                onSelect={handleStepTypeSelect}
                onCancel={() => setAddingStepAt(null)}
              />
            )}
            readonly={readonly}
          />

          {/* Step Detail - shows selected step configuration */}
          {selectedStep && (
            <PipelineStepDetail
              step={selectedStep}
              stepIndex={selectedStepIndex}
              totalSteps={sortedSteps.length}
              onUpdate={(updatedStep) => handleStepUpdate(selectedStep, updatedStep)}
              onSave={() => handleStepSave(selectedStep)}
              onRemove={() => handleStepRemove(selectedStep)}
              onExecute={() => handleStepExecute(selectedStep)}
              readonly={readonly}
              isDirty={selectedStep.id ? dirtySteps.has(selectedStep.id) : true}
            />
          )}
        </>
      )}

      {sortedSteps.length === 0 && !readonly && (
        <div className={styles.emptyState}>
          <button
            className={styles.addFirstStepButton}
            onClick={() => handleAddStepClick(0)}
            type="button"
          >
            + Add First Step
          </button>
        </div>
      )}

      {sortedSteps.length === 0 && readonly && (
        <div className={styles.emptyState}>
          No steps in pipeline.
        </div>
      )}
    </div>
  );
};
