import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerateTracks, useApproveGeneration, useDisapproveGeneration } from '../hooks/useQuizData.ts';
import type {GenerationDto} from '../types';
import { GenerationTracks } from './GenerationTracks.tsx';
import styles from '../MusicQuizApp.module.css';

export const GameDetails = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const [selectedGeneration, setSelectedGeneration] = useState<GenerationDto | null>(null);
  const [targetCount, setTargetCount] = useState(10);

  const { data: game, isLoading } = useGame(Number(gameId));
  const generateTracksMutation = useGenerateTracks();
  const approveGenerationMutation = useApproveGeneration();
  const disapproveGenerationMutation = useDisapproveGeneration();

  const handleGenerateTracks = () => {
    if (!gameId) return;
    generateTracksMutation.mutate({ gameId: Number(gameId), targetCount });
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
      <h1 className={styles.title}>
        Game {game.id}
        {approvedGenerations.length > 0 && (
          <span className={styles.approvedBadge}>
            ({approvedGenerations.length} approved)
          </span>
        )}
      </h1>
      
      <div className={styles.generationSettings}>
        <h2>Generate New Tracks</h2>
        <div>
          <label>
            Target Count:
            <input 
              type="number" 
              value={targetCount} 
              onChange={(e) => setTargetCount(Number(e.target.value))}
              min="1"
            />
          </label>
          <button 
            className={styles.button}
            onClick={handleGenerateTracks}
            disabled={generateTracksMutation.isPending}
          >
            {generateTracksMutation.isPending ? 'Generating...' : 'Generate'}
          </button>
        </div>
      </div>

      <div className="generations">
        <h2>Generations</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Target Count</th>
              <th>Status</th>
              <th>Approved</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {game.generations.map((generation) => (
              <tr 
                key={generation.id} 
                onClick={() => setSelectedGeneration(generation)}
                className={`
                  ${selectedGeneration?.id === generation.id ? styles.selected : ''}
                  ${generation.approved ? styles.approved : ''}
                `}
              >
                <td>{generation.id}</td>
                <td>{generation.targetCount}</td>
                <td>{generation.status}</td>
                <td>{generation.approved ? 'Yes' : 'No'}</td>
                <td>{new Date(generation.createdAt).toLocaleString()}</td>
                <td onClick={(e) => e.stopPropagation()}>
                  {generation.approved ? (
                    <button 
                      className={styles.unapproveButton}
                      onClick={() => handleDisapproveGeneration(generation.id)}
                      disabled={disapproveGenerationMutation.isPending}
                    >
                      {disapproveGenerationMutation.isPending ? 'Disapproving...' : 'Disapprove'}
                    </button>
                  ) : (
                    <button 
                      className={styles.approveButton}
                      onClick={() => handleApproveGeneration(generation.id)}
                      disabled={approveGenerationMutation.isPending}
                    >
                      {approveGenerationMutation.isPending ? 'Approving...' : 'Approve'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedGeneration && (
        <GenerationTracks generation={selectedGeneration} />
      )}
    </div>
  );
};
