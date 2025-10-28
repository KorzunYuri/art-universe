import { useState } from 'react';
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
import { BlacklistFilterStep } from '../steps/BlacklistFilterStep.tsx';
import { WhitelistFilterStep } from '../steps/WhitelistFilterStep.tsx';
import { FinalLimiterStep } from '../steps/FinalLimiterStep.tsx';
import { FinalCategoriesBalancerStep } from '../steps/FinalCategoriesBalancerStep.tsx';
import { SimpleStep } from '../steps/SimpleStep.tsx';
import styles from './PipelineEditor.module.scss';

interface PipelineEditorProps {
  pipeline: PipelineDto;
  onPipelineUpdate: (pipeline: PipelineDto) => void;
}

export const PipelineEditor = ({ pipeline, onPipelineUpdate }: PipelineEditorProps) => {
  const [localSteps, setLocalSteps] = useState<PipelineStepDto[]>(pipeline.steps);
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

  const handleStepUpdate = (stepIndex: number, updatedStep: PipelineStepDto) => {
    const newSteps = [...localSteps];
    newSteps[stepIndex] = updatedStep;
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
    } catch (error) {
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
    } catch (error) {
      showNotification('error', 'Failed to remove step');
    }
  };

  const handleStepMove = async (step: PipelineStepDto, direction: 'up' | 'down') => {
    if (!step.id) return;

    const currentIndex = sortedSteps.findIndex(s => s.id === step.id);
    const newPosition = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
    
    if (newPosition < 0 || newPosition >= sortedSteps.length) return;

    try {
      const updatedPipeline = await moveStepMutation.mutateAsync({
        pipelineId: pipeline.id,
        stepId: step.id,
        newPosition
      });
      onPipelineUpdate(updatedPipeline);
      setLocalSteps(updatedPipeline.steps);
      showNotification('success', 'Step moved successfully');
    } catch (error) {
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
    } catch (error) {
      showNotification('error', 'Failed to execute step');
    }
  };

  const renderStep = (step: PipelineStepDto, index: number) => {
    const isDirty = step.id ? dirtySteps.has(step.id) : true;
    const canMoveUp = index > 0 && step.id;
    const canMoveDown = index < sortedSteps.length - 1 && step.id;

    switch (step.type) {
      case 'BLACKLIST_FILTER':
        return (
          <BlacklistFilterStep
            key={step.id || `temp-${index}`}
            step={step}
            onUpdate={(updatedStep) => handleStepUpdate(index, updatedStep)}
            onSave={() => handleStepSave(step)}
            onRemove={() => handleStepRemove(step)}
            onMoveUp={canMoveUp ? () => handleStepMove(step, 'up') : undefined}
            onMoveDown={canMoveDown ? () => handleStepMove(step, 'down') : undefined}
            onExecute={() => handleStepExecute(step)}
            isDirty={isDirty}
          />
        );
      case 'WHITELIST_FILTER':
        return (
          <WhitelistFilterStep
            key={step.id || `temp-${index}`}
            step={step}
            onUpdate={(updatedStep) => handleStepUpdate(index, updatedStep)}
            onSave={() => handleStepSave(step)}
            onRemove={() => handleStepRemove(step)}
            onMoveUp={canMoveUp ? () => handleStepMove(step, 'up') : undefined}
            onMoveDown={canMoveDown ? () => handleStepMove(step, 'down') : undefined}
            onExecute={() => handleStepExecute(step)}
            isDirty={isDirty}
          />
        );
      case 'FINAL_LIMITER':
        return (
          <FinalLimiterStep
            key={step.id || `temp-${index}`}
            step={step}
            onUpdate={(updatedStep) => handleStepUpdate(index, updatedStep)}
            onSave={() => handleStepSave(step)}
            onRemove={() => handleStepRemove(step)}
            onMoveUp={canMoveUp ? () => handleStepMove(step, 'up') : undefined}
            onMoveDown={canMoveDown ? () => handleStepMove(step, 'down') : undefined}
            onExecute={() => handleStepExecute(step)}
            isDirty={isDirty}
          />
        );
      case 'FINAL_CATEGORIES_BALANCER':
        return (
          <FinalCategoriesBalancerStep
            key={step.id || `temp-${index}`}
            step={step}
            onUpdate={(updatedStep) => handleStepUpdate(index, updatedStep)}
            onSave={() => handleStepSave(step)}
            onRemove={() => handleStepRemove(step)}
            onMoveUp={canMoveUp ? () => handleStepMove(step, 'up') : undefined}
            onMoveDown={canMoveDown ? () => handleStepMove(step, 'down') : undefined}
            onExecute={() => handleStepExecute(step)}
            isDirty={isDirty}
          />
        );
      default:
        return (
          <SimpleStep
            key={step.id || `temp-${index}`}
            step={step}
            onUpdate={(updatedStep) => handleStepUpdate(index, updatedStep)}
            onSave={() => handleStepSave(step)}
            onRemove={() => handleStepRemove(step)}
            onMoveUp={canMoveUp ? () => handleStepMove(step, 'up') : undefined}
            onMoveDown={canMoveDown ? () => handleStepMove(step, 'down') : undefined}
            onExecute={() => handleStepExecute(step)}
            isDirty={isDirty}
          />
        );
    }
  };

  const getAvailablePositions = (position: number): StepPosition[] => {
    const positions: StepPosition[] = [];
    
    if (position === 0) {
      positions.push('START');
    }
    
    positions.push('MIDDLE');
    
    if (position === sortedSteps.length) {
      positions.push('FINAL');
    }
    
    return positions;
  };

  return (
    <div className={styles.editor}>
      <div className={styles.validation}>
        {!validation.isValid && (
          <div className={styles.validationErrors}>
            {validation.errors.map((error, index) => (
              <div key={index} className={styles.validationError}>{error}</div>
            ))}
          </div>
        )}
      </div>

      <div className={styles.steps}>
        {/* Add button at start */}
        {addingStepAt === 0 ? (
          <StepTypeSelector
            allowedPositions={getAvailablePositions(0)}
            onSelect={handleStepTypeSelect}
            onCancel={() => setAddingStepAt(null)}
          />
        ) : (
          <button 
            className={styles.addButton}
            onClick={() => handleAddStepClick(0)}
          >
            +
          </button>
        )}

        {/* Render steps with add buttons between them */}
        {sortedSteps.map((step, index) => (
          <>
            {renderStep(step, index)}
            
            {/* Add button after each step */}
            {addingStepAt === index + 1 ? (
              <StepTypeSelector
                allowedPositions={getAvailablePositions(index + 1)}
                onSelect={handleStepTypeSelect}
                onCancel={() => setAddingStepAt(null)}
              />
            ) : (
              <button 
                className={styles.addButton}
                onClick={() => handleAddStepClick(index + 1)}
              >
                +
              </button>
            )}
          </>
        ))}
      </div>
    </div>
  );
};
