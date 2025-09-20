import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerateTracks, useApproveGeneration, useDisapproveGeneration } from '../../hooks/useQuizData.ts';
import type {GenerationDto} from '../../types';
import type { GenerationStepUI, StepType } from '../../types/generation-steps';
import { StepRegistry } from '../../types/step-registry';
import { GenerationTracks } from '../GenerationTracks.tsx';
import { GenerationsList } from '../GenerationsList/GenerationsList.tsx';
import '../steps'; // Import to register steps
import React from 'react';
import styles from './GameDetails.module.scss';
import commonStyles from '../../MusicQuizApp.module.scss';

export const GameDetails = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const [selectedGeneration, setSelectedGeneration] = useState<GenerationDto | null>(null);
  const [generationSteps, setGenerationSteps] = useState<GenerationStepUI[]>([]);
  const [selectedStepType, setSelectedStepType] = useState<StepType>('WHITELIST_FILTER');

  const { data: game, isLoading } = useGame(Number(gameId));
  const generateTracksMutation = useGenerateTracks();
  const approveGenerationMutation = useApproveGeneration();
  const disapproveGenerationMutation = useDisapproveGeneration();

  const hasFinalStep = generationSteps.some(step => {
    const stepDef = StepRegistry.get(step.type);
    return stepDef?.isFinal;
  });
  const canAddSteps = !hasFinalStep;
  const availableSteps = StepRegistry.getAvailableSteps(generationSteps);

  const handleGenerateTracks = () => {
    if (!gameId) return;
    
    const steps = generationSteps.map(step => ({
      type: step.type,
      params: step.type === 'BLACKLIST_FILTER' 
        ? { categoryIds: step.categoryIds }
        : step.type === 'FINAL_SELECTION'
        ? { targetCount: step.targetCount }
        : step.type === 'FINAL_CATEGORIES_BALANCER'
        ? { 
            categories: step.categories?.map(cat => ({ id: cat.id, weight: cat.weight })),
            defaultQuota: step.defaultQuota,
            targetCount: step.targetCount 
          }
        : { categories: step.categories?.map(cat => ({ id: cat.id, weight: cat.weight })) }
    }));

    generateTracksMutation.mutate({ 
      gameId: Number(gameId), 
      steps 
    });
  };

  const handleStepUpdate = (stepId: string, updatedStep: GenerationStepUI) => {
    setGenerationSteps(prev => prev.map(s => s.id === stepId ? updatedStep : s));
  };

  const handleStepRemove = (stepId: string) => {
    setGenerationSteps(prev => prev.filter(step => step.id !== stepId));
  };

  const handleAddStep = () => {
    const stepDef = StepRegistry.get(selectedStepType);
    if (!stepDef) return;
    
    const newStep = stepDef.createDefault();
    setGenerationSteps(prev => [...prev, newStep]);
  };

  const handleApproveGeneration = (generationId: number) => {
    approveGenerationMutation.mutate({ generationId });
  };

  const handleDisapproveGeneration = (generationId: number) => {
    disapproveGenerationMutation.mutate({ generationId });
  };

  if (isLoading) return <div>Loading...</div>;
  if (!game) return <div>Game not found</div>;

  const approvedGenerations = game.generations.filter(g => g.approved);

  return (
    <div>
      <h1 className={commonStyles.title}>
        Game {game.id}
        {approvedGenerations.length > 0 && (
          <span className={commonStyles.approvedBadge}>
            ({approvedGenerations.length} approved)
          </span>
        )}
      </h1>
      
      <div className={styles.generationSettings}>
        <h2>Generate New Tracks</h2>

        <div className={styles.stepsBuilder}>
          <div className={styles.stepsGrid}>
            {generationSteps.map((step, index) => {
              const stepDef = StepRegistry.get(step.type);
              if (!stepDef) return null;
              
              const StepComponent = stepDef.component;
              
              return (
                <React.Fragment key={step.id}>
                  <StepComponent
                    step={step}
                    onUpdate={(updatedStep) => handleStepUpdate(step.id, updatedStep)}
                    onRemove={() => handleStepRemove(step.id)}
                  />
                  {index < generationSteps.length - 1 && (
                    <div className={styles.pipelineArrow}>→</div>
                  )}
                </React.Fragment>
              );
            })}
          </div>
        </div>

        <div className={styles.generateSection}>
          {canAddSteps && (
            <div className={styles.addStepSection}>
              <select 
                value={selectedStepType} 
                onChange={(e) => setSelectedStepType(e.target.value as StepType)}
              >
                {availableSteps.map(stepDef => (
                  <option key={stepDef.type} value={stepDef.type}>
                    {stepDef.label}
                  </option>
                ))}
              </select>
              <button 
                className={commonStyles.button}
                onClick={handleAddStep}
              >
                Add Step
              </button>
            </div>
          )}
          <button 
            className={commonStyles.button}
            onClick={handleGenerateTracks}
            disabled={generateTracksMutation.isPending || generationSteps.length === 0 || !hasFinalStep}
          >
            {generateTracksMutation.isPending ? 'Generating...' : 'Generate'}
          </button>
        </div>
      </div>

      <div className={styles.tablesContainer}>
        <GenerationsList
          generations={game.generations}
          selectedGeneration={selectedGeneration}
          onGenerationSelect={setSelectedGeneration}
          onApprove={handleApproveGeneration}
          onDisapprove={handleDisapproveGeneration}
          isApprovePending={approveGenerationMutation.isPending}
          isDisapprovePending={disapproveGenerationMutation.isPending}
        />

        {selectedGeneration && (
          <GenerationTracks generation={selectedGeneration} />
        )}
      </div>
    </div>
  );
};
