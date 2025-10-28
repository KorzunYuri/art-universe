import styles from './StepStats.module.scss';

interface StepStatsProps {
  resultStats?: string;
}

export const StepStats = ({ resultStats }: StepStatsProps) => {
  if (!resultStats) {
    return null;
  }

  return (
    <div className={styles.stats}>
      <div className={styles.label}>Result Stats:</div>
      <pre className={styles.content}>
        {resultStats}
      </pre>
    </div>
  );
};
