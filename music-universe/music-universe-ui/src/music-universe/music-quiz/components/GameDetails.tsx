import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame, useGenerateTracks, useApproveGeneration } from '../hooks/useQuizData.ts';
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

  const handleGenerateTracks = () => {
    if (!gameId || game?.generationId) return;
    generateTracksMutation.mutate({ gameId: Number(gameId), targetCount });
  };

  const handleApproveGeneration = (generationId: number) => {
    if (!gameId) return;
    approveGenerationMutation.mutate({ gameId: Number(gameId), generationId });
  };

  if (isLoading) return <div>Loading...</div>;
  if (!game) return <div>Game not found</div>;

  const isApproved = !!game.generationId;

  return (
    <div>
      <h1 className={isApproved ? styles.approvedTitle : styles.title}>
        Game {game.id} {isApproved && '(Approved)'}
      </h1>
      
      {!isApproved && (
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
      )}

      <div className="generations">
        <h2>Generations</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Target Count</th>
              <th>Status</th>
              <th>Created At</th>
              {!isApproved && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {game.generations.map((generation) => (
              <tr 
                key={generation.id} 
                onClick={() => setSelectedGeneration(generation)}
                className={`
                  ${selectedGeneration?.id === generation.id ? 'selected' : ''}
                  ${generation.id === game.generationId ? 'approved' : ''}
                `}
              >
                <td>{generation.id}</td>
                <td>{generation.targetCount}</td>
                <td>{generation.status}</td>
                <td>{new Date(generation.createdAt).toLocaleString()}</td>
                {!isApproved && (
                  <td onClick={(e) => e.stopPropagation()}>
                    <button 
                      className={styles.approveButton}
                      onClick={() => handleApproveGeneration(generation.id)}
                      disabled={approveGenerationMutation.isPending}
                    >
                      {approveGenerationMutation.isPending ? 'Approving...' : 'Approve'}
                    </button>
                  </td>
                )}
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
