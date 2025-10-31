import { useState } from 'react';
import type { PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import styles from './StepStats.module.scss';

interface StepStatsProps {
  step: PipelineStepDto;
}

interface BasicStepStats {
  inputRecords?: number;
  filteredRecords?: number;
  outputRecords?: number;
  inputArtists?: number;
  filteredArtists?: number;
  outputArtists?: number;
  executionTimeMs?: number;
}

export const StepStats = ({ step }: StepStatsProps) => {
  const [isExpanded, setIsExpanded] = useState(false);

  if (!step.resultStats) {
    return null;
  }

  let stats: BasicStepStats;
  try {
    stats = JSON.parse(step.resultStats);
  } catch {
    return null;
  }

  const tracksDelta = (stats.outputRecords || 0) - (stats.inputRecords || 0);
  const artistsDelta = (stats.outputArtists || 0) - (stats.inputArtists || 0);

  return (
    <div className={styles.stats}>
      <button 
        className={styles.statsButton}
        onClick={() => setIsExpanded(!isExpanded)}
      >
        Stats
      </button>
      
      {isExpanded && (
        <div className={styles.content}>
          <table className={styles.statsTable}>
            <thead>
              <tr>
                <th></th>
                <th>In</th>
                <th>Δ</th>
                <th>Out</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Tracks</td>
                <td>{stats.inputRecords || 0}</td>
                <td>{tracksDelta >= 0 ? '+' : ''}{tracksDelta}</td>
                <td>{stats.outputRecords || 0}</td>
              </tr>
              <tr>
                <td>Artists</td>
                <td>{stats.inputArtists || 0}</td>
                <td>{artistsDelta >= 0 ? '+' : ''}{artistsDelta}</td>
                <td>{stats.outputArtists || 0}</td>
              </tr>
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
