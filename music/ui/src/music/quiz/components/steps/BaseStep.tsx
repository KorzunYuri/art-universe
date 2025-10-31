import {type ReactNode, useState} from 'react';
import {type PipelineStepDto} from '@/music/quiz/types/pipeline-steps.ts';
import {useStepPreview} from '@/music/quiz/hooks/useQuizData.ts';
import styles from './BaseStep.module.scss';

interface BaseStepProps {
  step: PipelineStepDto;
  title: string;
  description?: string;
  children?: ReactNode;
  onRemove?: () => void;
  onMoveUp?: () => void;
  onMoveDown?: () => void;
  onSave?: () => void;
  onExecute?: () => void;
  readonly?: boolean;
  isDirty?: boolean;
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

export const BaseStep = ({ 
  step,
  title,
  description,
  children,
  onRemove, 
  onMoveUp, 
  onMoveDown,
  onSave,
  onExecute,
  readonly = false,
  isDirty = false
}: BaseStepProps) => {
  const [showPreview, setShowPreview] = useState(false);
  const [showStats, setShowStats] = useState(false);
  
  const { data: livePreviewData, isLoading, error } = useStepPreview(step.id || 0, showPreview && !!step.id);

  const handlePreviewClick = () => {
    setShowPreview(!showPreview);
  };

  const handleStatsClick = () => {
    setShowStats(!showStats);
  };

  let stats: BasicStepStats | null = null;
  let hasStats: null | boolean = false;
  if (step.resultStats && step.resultStats.trim()) {
    try {
      stats = JSON.parse(step.resultStats);
      hasStats = stats && Object.keys(stats).length > 0;
    } catch {
      hasStats = false;
    }
  }

  const tracksDelta = stats ? (stats.outputRecords || 0) - (stats.inputRecords || 0) : 0;
  const artistsDelta = stats ? (stats.outputArtists || 0) - (stats.inputArtists || 0) : 0;

  const displayPreviewData = showPreview ? livePreviewData : step.previewData;
  const displayPreviewText = error ? 'Failed to load preview' : (displayPreviewData || '{}');

  return (
    <div className={`${styles.step} ${readonly ? styles.readonly : ''} ${isDirty ? styles.dirty : ''}`}>
      <div className={styles.header}>
        <h4>{title}</h4>
        {!readonly && onRemove && (
          <button className={styles.removeButton} onClick={onRemove}>×</button>
        )}
      </div>

      {description && (
        <div className={styles.description}>
          <span>{description}</span>
        </div>
      )}

      {children && (
        <div className={styles.configuration}>
          {children}
        </div>
      )}

      {step.id && (
        <div className={styles.actionPanel}>
          <button 
            className={`${styles.actionButton} ${showPreview ? styles.active : ''}`}
            onClick={handlePreviewClick}
            disabled={isLoading}
          >
            {isLoading ? 'Loading...' : 'Preview'}
          </button>
          <button 
            className={`${styles.actionButton} ${showStats ? styles.active : ''}`}
            onClick={handleStatsClick}
            disabled={!hasStats}
          >
            Stats
          </button>
          {!readonly && onExecute && (
            <button className={styles.actionButton} onClick={onExecute}>
              Execute
            </button>
          )}
        </div>
      )}

      {showPreview && step.id && (
        <div className={styles.expandedPanel}>
          <pre className={styles.panelContent}>
            {typeof displayPreviewText === 'string' ? displayPreviewText : JSON.stringify(displayPreviewText, null, 2)}
          </pre>
        </div>
      )}

      {showStats && stats && (
        <div className={styles.expandedPanel}>
          <table className={styles.statsTable}>
            <thead>
              <tr>
                <th>Stats</th>
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

      {!readonly && (onMoveUp || onMoveDown) && (
        <div className={styles.moveActions}>
          {onMoveUp && (
            <button className={styles.moveButton} onClick={onMoveUp}>←</button>
          )}
          {onMoveDown && (
            <button className={styles.moveButton} onClick={onMoveDown}>→</button>
          )}
        </div>
      )}

      {!readonly && onSave && isDirty && (
        <div className={styles.saveAction}>
          <button className={styles.saveButton} onClick={onSave}>Save</button>
        </div>
      )}
    </div>
  );
};
