import { type PipelineStepDto, STEP_LABELS } from '@/music/quiz/types/pipeline-steps.ts';
import { getStepDescription } from '@/music/quiz/utils/stepDescriptions.ts';
import { FinalLimiterConfig } from '@/music/quiz/components/stepConfigs/FinalLimiterConfig.tsx';
import { FinalCategoriesBalancerConfig } from '@/music/quiz/components/stepConfigs/FinalCategoriesBalancerConfig.tsx';
import { WhitelistFilterConfig } from '@/music/quiz/components/stepConfigs/WhitelistFilterConfig.tsx';
import { BlacklistFilterConfig } from '@/music/quiz/components/stepConfigs/BlacklistFilterConfig.tsx';
import styles from './PipelineStepDetail.module.scss';

interface PipelineStepDetailProps {
  step: PipelineStepDto;
  stepIndex: number;
  totalSteps: number;
  onUpdate: (updatedStep: PipelineStepDto) => void;
  onSave: () => void;
  onRemove: () => void;
  onMoveLeft?: () => void;
  onMoveRight?: () => void;
  onExecute: () => void;
  readonly?: boolean;
  isDirty?: boolean;
}

export const PipelineStepDetail = ({
  step,
  stepIndex,
  totalSteps,
  onUpdate,
  onSave,
  onRemove,
  onMoveLeft,
  onMoveRight,
  onExecute,
  readonly = false,
  isDirty = false
}: PipelineStepDetailProps) => {
  const stepLabel = STEP_LABELS[step.type] || step.type;
  const stepLogic = getStepDescription(step.type);

  // Render step configuration component (only the config UI, not the full step)
  const renderStepConfiguration = () => {
    const configProps = {
      step,
      onUpdate,
      readonly
    };

    switch (step.type) {
      case 'FINAL_LIMITER':
        return <FinalLimiterConfig {...configProps} />;
      case 'FINAL_CATEGORIES_BALANCER':
        return <FinalCategoriesBalancerConfig {...configProps} />;
      case 'WHITELIST_FILTER':
        return <WhitelistFilterConfig {...configProps} />;
      case 'BLACKLIST_FILTER':
        return <BlacklistFilterConfig {...configProps} />;
      // Steps without configuration
      case 'START_DATASOURCE':
      case 'APPROVED_FILTER':
      case 'TRACK_RECENCY_PENALTY':
      case 'ARTIST_RECENCY_PENALTY':
      case 'ARTIST_DIVERSITY':
        return <div className={styles.noConfig}>-</div>;
      default:
        return <div className={styles.noConfig}>-</div>;
    }
  };

  // Parse result stats for display
  const getStatsDisplay = () => {
    if (!step.resultStats) {
      return null;
    }

    try {
      const stats = JSON.parse(step.resultStats);
      const tracksDelta = (stats.outputRecords ?? 0) - (stats.inputRecords ?? 0);
      const artistsDelta = (stats.outputArtists ?? 0) - (stats.inputArtists ?? 0);

      return {
        artists: {
          input: stats.inputArtists ?? 0,
          delta: artistsDelta,
          output: stats.outputArtists ?? 0,
        },
        tracks: {
          input: stats.inputRecords ?? 0,
          delta: tracksDelta,
          output: stats.outputRecords ?? 0,
        },
        executionTime: stats.executionTimeMs,
      };
    } catch {
      return null;
    }
  };

  const stats = getStatsDisplay();

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        {/* Step Name and Logic Explanation */}
        <div className={styles.titleSection}>
          <div className={styles.titleContent}>
            <h2 className={styles.stepName}>{stepLabel}</h2>
            <div className={styles.stepLogic}>{stepLogic}</div>
          </div>
          {!readonly && onExecute && (
            <button
              className={styles.executeButton}
              onClick={onExecute}
              type="button"
            >
              Execute
            </button>
          )}
        </div>

        {/* Horizontal sections: Configuration, Statistics */}
        <div className={styles.horizontalSections}>
          {/* Configuration Section */}
          <div className={styles.section}>
            <h3 className={styles.sectionTitle}>Configuration</h3>
            <div className={styles.configContent}>
              {renderStepConfiguration()}
            </div>
          </div>

          {/* Statistics Section */}
          <div className={styles.section}>
            <h3 className={styles.sectionTitle}>Statistics</h3>
            {stats ? (
              <div className={styles.statsDisplay}>
                <table className={styles.statsTable}>
                  <thead>
                    <tr>
                      <th></th>
                      <th>Input</th>
                      <th>Delta</th>
                      <th>Output</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>Artists</td>
                      <td>{stats.artists.input}</td>
                      <td className={stats.artists.delta >= 0 ? styles.positive : styles.negative}>
                        {stats.artists.delta >= 0 ? `+${stats.artists.delta}` : stats.artists.delta}
                      </td>
                      <td>{stats.artists.output}</td>
                    </tr>
                    <tr>
                      <td>Tracks</td>
                      <td>{stats.tracks.input}</td>
                      <td className={stats.tracks.delta >= 0 ? styles.positive : styles.negative}>
                        {stats.tracks.delta >= 0 ? `+${stats.tracks.delta}` : stats.tracks.delta}
                      </td>
                      <td>{stats.tracks.output}</td>
                    </tr>
                  </tbody>
                </table>
                {stats.executionTime !== undefined && (
                  <div className={styles.executionTime}>
                    Time: {stats.executionTime}ms
                  </div>
                )}
              </div>
            ) : (
              <div className={styles.noStats}>Not executed</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};