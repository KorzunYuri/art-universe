import { useState, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerations, useGenerateTracks, useApproveGeneration, useDisapproveGeneration, useGenerationPipeline } from '@/music/quiz/hooks/useQuizData.ts';
import { useNotifications } from '@/music/shared/hooks/useNotifications.ts';
import { validatePipeline, type PipelineDto } from '@/music/quiz/types/pipeline-steps.ts';
import type {GenerationDto} from '../../types';
import { PipelineEditor } from '../PipelineEditor/PipelineEditor.tsx';
import { GenerationTracks } from '../GenerationTracks.tsx';
import { GenerationsList } from '../GenerationsList/GenerationsList.tsx';
import { PipelineTabs, type PipelineTab } from '../PipelineTabs/PipelineTabs.tsx';
import React from 'react';
import styles from './GameDetails.module.scss';
import commonStyles from '../../MusicQuizApp.module.scss';

export const GameDetails = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const [selectedGeneration, setSelectedGeneration] = useState<GenerationDto | null>(null);
  const [currentPipeline, setCurrentPipeline] = useState<PipelineDto | null>(null);
  const [activeTabId, setActiveTabId] = useState<string>('game');
  const [openGenerationTabs, setOpenGenerationTabs] = useState<Map<number, number>>(new Map());

  const { data: game, isLoading: gameLoading } = useGame(Number(gameId));
  const { data: generations, isLoading: generationsLoading } = useGenerations(Number(gameId));
  const { showNotification } = useNotifications();
  const generateTracksMutation = useGenerateTracks();
  const approveGenerationMutation = useApproveGeneration();
  const disapproveGenerationMutation = useDisapproveGeneration();

  // Get the active generation ID from the active tab
  const activeGenerationId = useMemo(() => {
    if (activeTabId === 'game') return undefined;
    const genId = parseInt(activeTabId.replace('generation-', ''));
    return isNaN(genId) ? undefined : genId;
  }, [activeTabId]);

  // Get the active generation's pipelineId
  const activeGeneration = useMemo(() => {
    if (!activeGenerationId || !generations) return undefined;
    return generations.find(g => g.id === activeGenerationId);
  }, [activeGenerationId, generations]);

  // Fetch generation pipeline if needed
  const { data: generationPipeline } = useGenerationPipeline(activeGeneration?.pipelineId);

  // Set pipeline when game loads
  React.useEffect(() => {
    if (game?.pipeline && !currentPipeline) {
      setCurrentPipeline(game.pipeline);
    }
  }, [game, currentPipeline]);

  const handlePipelineUpdate = (updatedPipeline: PipelineDto) => {
    setCurrentPipeline(updatedPipeline);
  };

  const handleGenerationClick = (generation: GenerationDto) => {
    const tabId = `generation-${generation.id}`;

    // Add to open tabs if not already open
    if (!openGenerationTabs.has(generation.id)) {
      setOpenGenerationTabs(new Map(openGenerationTabs).set(generation.id, generation.pipelineId));
    }

    // Switch to this tab
    setActiveTabId(tabId);
    setSelectedGeneration(generation);
  };

  const handleTabClose = (tabId: string) => {
    const genId = parseInt(tabId.replace('generation-', ''));
    if (isNaN(genId)) return;

    // Remove from open tabs
    const newTabs = new Map(openGenerationTabs);
    newTabs.delete(genId);
    setOpenGenerationTabs(newTabs);

    // If closing active tab, switch to game tab
    if (tabId === activeTabId) {
      setActiveTabId('game');
      setSelectedGeneration(null);
    }
  };

  const handleTabSelect = (tabId: string) => {
    setActiveTabId(tabId);

    if (tabId === 'game') {
      setSelectedGeneration(null);
    } else {
      const genId = parseInt(tabId.replace('generation-', ''));
      const generation = generations?.find(g => g.id === genId);
      if (generation) {
        setSelectedGeneration(generation);
      }
    }
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

  // Build tabs array
  const tabs: PipelineTab[] = [
    {
      id: 'game',
      label: `Game ${game.id} Pipeline`,
      type: 'game',
    },
    ...Array.from(openGenerationTabs.entries()).map(([genId, pipelineId]) => {
      const gen = generations?.find(g => g.id === genId);
      return {
        id: `generation-${genId}`,
        label: `Generation #${genId}${gen?.approved ? ' ✓' : ''}`,
        type: 'generation' as const,
        pipelineId,
        generationId: genId,
      };
    }),
  ];

  // Determine which pipeline to display
  const displayPipeline = activeTabId === 'game' ? currentPipeline : generationPipeline;

  const validation = displayPipeline ? validatePipeline(displayPipeline.steps) : { isValid: false, errors: [] };
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

        {/* Pipeline Tabs */}
        {tabs.length > 0 && (
          <PipelineTabs
            tabs={tabs}
            activeTabId={activeTabId}
            onTabSelect={handleTabSelect}
            onTabClose={handleTabClose}
          />
        )}

        {/* Pipeline Editor */}
        {displayPipeline && (
          <PipelineEditor
            pipeline={displayPipeline}
            onPipelineUpdate={handlePipelineUpdate}
          />
        )}

        {/* Generate Button - only show for game pipeline */}
        {activeTabId === 'game' && (
          <div className={styles.generateSection}>
            <button
              className={commonStyles.button}
              onClick={handleGenerateTracks}
              disabled={generateTracksMutation.isPending || !validation.isValid}
            >
              {generateTracksMutation.isPending ? 'Generating...' : 'Generate Tracks'}
            </button>
          </div>
        )}
      </div>

      {generationsLoading ? (
        <div>Loading generations...</div>
      ) : generations && generations.length > 0 ? (
        <div className={styles.tablesContainer}>
          <GenerationsList
            generations={generations}
            selectedGeneration={selectedGeneration}
            onGenerationSelect={setSelectedGeneration}
            onGenerationClick={handleGenerationClick}
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
