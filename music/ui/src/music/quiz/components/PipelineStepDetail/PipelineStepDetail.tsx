import { type PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';
import { stepRegistry } from '@/music/quiz/steps';
import { getStepConfigComponent } from '@/music/quiz/components/stepConfigs/stepComponentConfigRegistry.ts';
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
  isSaving?: boolean;
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
  isDirty = false,
  isSaving = false
}: PipelineStepDetailProps) => {
  const stepInstance = stepRegistry.get(step.type);
  const stepLabel = stepInstance.getLabel();
  const stepLogic = stepInstance.getDescription();

  // Render step configuration component (only the config UI, not the full step)
  const renderStepConfiguration = () => {
    const ConfigComponent = getStepConfigComponent(step.type);

    if (!ConfigComponent) {
      // Config-free steps
      return <div className={styles.noConfig}>-</div>;
    }

    return <ConfigComponent step={step} onUpdate={onUpdate} readonly={readonly || isSaving} />;
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
            {!readonly && isDirty && (
              <button
                className={styles.saveButton}
                onClick={onSave}
                type="button"
                disabled={isSaving}
              >
                {isSaving ? 'Saving...' : 'Save'}
              </button>
            )}
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
                      <td className={stats.artists.delta === 0 ? styles.neutral : (stats.artists.delta > 0 ? styles.positive : styles.negative)}>
                        {stats.artists.delta === 0 ? '0' : (stats.artists.delta > 0 ? `+${stats.artists.delta}` : stats.artists.delta)}
                      </td>
                      <td>{stats.artists.output}</td>
                    </tr>
                    <tr>
                      <td>Tracks</td>
                      <td>{stats.tracks.input}</td>
                      <td className={stats.tracks.delta === 0 ? styles.neutral : (stats.tracks.delta > 0 ? styles.positive : styles.negative)}>
                        {stats.tracks.delta === 0 ? '0' : (stats.tracks.delta > 0 ? `+${stats.tracks.delta}` : stats.tracks.delta)}
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