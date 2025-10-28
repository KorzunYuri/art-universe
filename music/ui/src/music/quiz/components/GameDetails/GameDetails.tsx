import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerations, useGenerateTracks, useApproveGeneration, useDisapproveGeneration } from '@/music/quiz/hooks/useQuizData.ts';
import { useNotifications } from '@/music/shared/hooks/useNotifications.ts';
import { validatePipeline, type PipelineDto } from '@/music/quiz/types/pipeline-steps.ts';
import type {GenerationDto} from '../../types';
import { PipelineEditor } from '../PipelineEditor/PipelineEditor.tsx';
import { GenerationTracks } from '../GenerationTracks.tsx';
import { GenerationsList } from '../GenerationsList/GenerationsList.tsx';
import React from 'react';
import styles from './GameDetails.module.scss';
import commonStyles from '../../MusicQuizApp.module.scss';

export const GameDetails = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const [selectedGeneration, setSelectedGeneration] = useState<GenerationDto | null>(null);
  const [currentPipeline, setCurrentPipeline] = useState<PipelineDto | null>(null);

  const { data: game, isLoading: gameLoading } = useGame(Number(gameId));
  const { data: generations, isLoading: generationsLoading } = useGenerations(Number(gameId));
  const { showNotification } = useNotifications();
  const generateTracksMutation = useGenerateTracks();
  const approveGenerationMutation = useApproveGeneration();
  const disapproveGenerationMutation = useDisapproveGeneration();

  // Set pipeline when game loads
  React.useEffect(() => {
    if (game?.pipeline && !currentPipeline) {
      setCurrentPipeline(game.pipeline);
    }
  }, [game, currentPipeline]);

  const handlePipelineUpdate = (updatedPipeline: PipelineDto) => {
    setCurrentPipeline(updatedPipeline);
  };

  const handleGenerateTracks = async () => {
    if (!gameId || !currentPipeline) return;
    
    const validation = validatePipeline(currentPipeline.steps);
    if (!validation.isValid) {
      showNotification('error', 'Pipeline is not valid for generation');
      return;
    }

    try {
      await generateTracksMutation.mutateAsync({ gameId: Number(gameId) });
      showNotification('success', 'Tracks generated successfully');
    } catch (error) {
      showNotification('error', 'Failed to generate tracks');
    }
  };

  const handleApproveGeneration = (generationId: number) => {
    approveGenerationMutation.mutate({ generationId });
  };

  const handleDisapproveGeneration = (generationId: number) => {
    disapproveGenerationMutation.mutate({ generationId });
  };

  if (gameLoading) return <div>Loading...</div>;
  if (!game) return <div>Game not found</div>;

  const validation = currentPipeline ? validatePipeline(currentPipeline.steps) : { isValid: false, errors: [] };
  const approvedGenerations = generations?.filter(g => g.approved) || [];

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
      
      <div className={styles.pipelineSection}>
        <h2>Pipeline Configuration</h2>
        {currentPipeline && (
          <PipelineEditor 
            pipeline={currentPipeline}
            onPipelineUpdate={handlePipelineUpdate}
          />
        )}
        
        <div className={styles.generateSection}>
          <button 
            className={commonStyles.button}
            onClick={handleGenerateTracks}
            disabled={generateTracksMutation.isPending || !validation.isValid}
          >
            {generateTracksMutation.isPending ? 'Generating...' : 'Generate Tracks'}
          </button>
        </div>
      </div>

      {generationsLoading ? (
        <div>Loading generations...</div>
      ) : generations && generations.length > 0 ? (
        <div className={styles.tablesContainer}>
          <GenerationsList
            generations={generations}
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
      ) : (
        <div>No generations yet. Generate tracks to create the first generation.</div>
      )}
    </div>
  );
};
