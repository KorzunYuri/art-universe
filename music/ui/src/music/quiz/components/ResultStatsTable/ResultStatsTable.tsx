import styles from './ResultStatsTable.module.scss';

interface ResultStats {
  inputRecords?: number;
  outputRecords?: number;
  inputArtists?: number;
  outputArtists?: number;
  filteredRecords?: number;
  filteredArtists?: number;
  executionTimeMs?: number;
}

interface ResultStatsTableProps {
  resultStats: string | null | undefined;
}

export const ResultStatsTable = ({ resultStats }: ResultStatsTableProps) => {
  if (!resultStats) {
    return (
      <div className={styles.placeholder}>
        No statistics available. Execute the step to see results.
      </div>
    );
  }

  let stats: ResultStats;
  try {
    stats = JSON.parse(resultStats) as ResultStats;
  } catch {
    return (
      <div className={styles.placeholder}>
        Invalid statistics data.
      </div>
    );
  }

  const tracksDelta = (stats.outputRecords ?? 0) - (stats.inputRecords ?? 0);
  const artistsDelta = (stats.outputArtists ?? 0) - (stats.inputArtists ?? 0);

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>Result Statistics</h3>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Entity</th>
            <th>Input</th>
            <th>Filtered</th>
            <th>Output</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td className={styles.entity}>Artists</td>
            <td className={styles.count}>{stats.inputArtists ?? 0}</td>
            <td className={`${styles.count} ${artistsDelta >= 0 ? styles.positive : styles.negative}`}>
              {artistsDelta >= 0 ? `+${artistsDelta}` : artistsDelta}
            </td>
            <td className={styles.count}>{stats.outputArtists ?? 0}</td>
          </tr>
          <tr>
            <td className={styles.entity}>Tracks</td>
            <td className={styles.count}>{stats.inputRecords ?? 0}</td>
            <td className={`${styles.count} ${tracksDelta >= 0 ? styles.positive : styles.negative}`}>
              {tracksDelta >= 0 ? `+${tracksDelta}` : tracksDelta}
            </td>
            <td className={styles.count}>{stats.outputRecords ?? 0}</td>
          </tr>
        </tbody>
      </table>

      {stats.executionTimeMs !== undefined && (
        <div className={styles.executionTime}>
          Execution time: <span className={styles.timeValue}>{stats.executionTimeMs}ms</span>
        </div>
      )}
    </div>
  );
};
