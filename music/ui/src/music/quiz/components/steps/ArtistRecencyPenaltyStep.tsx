import type { StepComponentProps } from '@/music/quiz/types/step-registry.ts';
import styles from '../StepBuilder/StepBuilder.module.scss';

export const ArtistRecencyPenaltyStep = ({ onRemove, readonly = false }: StepComponentProps) => {
  return (
    <div className={`${styles.builder} ${styles.inline} ${readonly ? styles.readonly : ''}`}>
      <div className={styles.header}>
        <h4>Artist Recency Penalty</h4>
        {!readonly && onRemove && (
          <button className={styles.removeButton} onClick={onRemove}>×</button>
        )}
      </div>

      <div className={styles.content}>
        <p>Applies penalty to recently used artists</p>
      </div>
    </div>
  );
};
