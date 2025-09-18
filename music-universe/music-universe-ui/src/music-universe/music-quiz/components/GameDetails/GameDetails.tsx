import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerateTracks, useApproveGeneration, useDisapproveGeneration } from '../../hooks/useQuizData.ts';
import type {GenerationDto} from '../../types';
import type { GenerationStepUI, StepType } from '../../types/generation-steps';
import { isFinalStep } from '../../types/generation-steps';
import { GenerationTracks } from '../GenerationTracks.tsx';
import { GenerationsList } from '../GenerationsList/GenerationsList.tsx';
import { StepBuilder } from '../StepBuilder/StepBuilder';
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

  const hasFinalStep = generationSteps.some(step => isFinalStep(step.type));
  const canAddSteps = !hasFinalStep;

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
    const newStep: GenerationStepUI = {
      id: `${selectedStepType.toLowerCase()}-${Date.now()}`,
      type: selectedStepType,
      ...(selectedStepType === 'BLACKLIST_FILTER' 
        ? { categoryIds: [] }
        : { categories: [] }
      ),
      ...(selectedStepType === 'FINAL_SELECTION' || selectedStepType === 'FINAL_CATEGORIES_BALANCER' 
        ? { targetCount: 10 } 
        : {}
      )
    };
    
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
            {generationSteps.map((step, index) => (
              <React.Fragment key={step.id}>
                <StepBuilder
                  stepType={step.type}
                  existingStep={step}
                  onStepUpdate={(updatedStep) => handleStepUpdate(step.id, updatedStep)}
                  onStepRemove={() => handleStepRemove(step.id)}
                  isInline={true}
                />
                {index < generationSteps.length - 1 && (
                  <div className={styles.pipelineArrow}>→</div>
                )}
              </React.Fragment>
            ))}
          </div>
        </div>

        <div className={styles.generateSection}>
          {canAddSteps && (
            <div className={styles.addStepSection}>
              <select 
                value={selectedStepType} 
                onChange={(e) => setSelectedStepType(e.target.value as StepType)}
              >
                <option value="WHITELIST_FILTER">Whitelist Filter</option>
                <option value="BLACKLIST_FILTER">Blacklist Filter</option>
                <option value="FINAL_SELECTION">Final Selection</option>
                <option value="FINAL_CATEGORIES_BALANCER">Final Categories Balancer</option>
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
