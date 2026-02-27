import { useGenerationTracks, useDeleteGenerationTrack } from '@/music/quiz/hooks/useQuizData.ts';
import type {GenerationDto} from '../types';
import styles from './GameTable/GameTable.module.scss';
import commonStyles from '../MusicQuizApp.module.scss';

interface Props {
  generation: GenerationDto;
}

export const GenerationTracks = ({ generation }: Props) => {
  const { data: tracks, isLoading } = useGenerationTracks(generation.id);
  const deleteTrackMutation = useDeleteGenerationTrack();

  const handleDeleteTrack = (trackId: number) => {
    deleteTrackMutation.mutate({ generationId: generation.id, trackId });
  };

  const copyToClipboard = () => {
    if (!tracks) return;
    const trackList = tracks
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map(track => `${track.orderIndex}. ${track.artistName} - ${track.trackName}`)
      .join('\n');
    
    navigator.clipboard.writeText(trackList);
  };

  if (isLoading) return <div>Loading tracks...</div>;
  if (!tracks) return <div>No tracks found</div>;

  return (
    <div className={styles.gameTable}>
      <div className={styles.header}>
        <h3>Generation {generation.id} Tracks (Target: {generation.targetCount})</h3>
        <button className={commonStyles.button} onClick={copyToClipboard}>Copy to Clipboard</button>
      </div>
      
      <table className={commonStyles.table}>
        <thead>
          <tr>
            <th>Order</th>
            <th>Artist</th>
            <th>Track</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tracks
            .sort((a, b) => a.orderIndex - b.orderIndex)
            .map((track) => (
              <tr key={track.trackId}>
                <td>{track.orderIndex}</td>
                <td>{track.artistName}</td>
                <td>{track.trackName}</td>
                <td>
                  <button
                    className={commonStyles.deleteButton}
                    onClick={() => handleDeleteTrack(track.trackId)}
                    disabled={deleteTrackMutation.isPending}
                    title="Delete track"
                  >
                    ✕
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};
