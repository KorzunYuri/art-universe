import type { GenerationDto } from '../../types';
import styles from '../GameTable/GameTable.module.scss';
import commonStyles from '../../MusicQuizApp.module.scss';

interface GenerationsListProps {
  generations: GenerationDto[];
  selectedGeneration: GenerationDto | null;
  onGenerationSelect: (generation: GenerationDto) => void;
  onApprove: (generationId: number) => void;
  onDisapprove: (generationId: number) => void;
  isApprovePending: boolean;
  isDisapprovePending: boolean;
}

export const GenerationsList = ({
  generations,
  selectedGeneration,
  onGenerationSelect,
  onApprove,
  onDisapprove,
  isApprovePending,
  isDisapprovePending
}: GenerationsListProps) => {
  return (
    <div className={styles.gameTable}>
      <div className={styles.header}>
        <h2>Generations</h2>
      </div>
      <table className={commonStyles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Status</th>
            <th>Approved</th>
            <th>Created At</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {generations.map((generation) => (
            <tr 
              key={generation.id} 
              onClick={() => onGenerationSelect(generation)}
              className={`
                ${selectedGeneration?.id === generation.id ? commonStyles.selected : ''}
                ${generation.approved ? commonStyles.approved : ''}
              `}
            >
              <td>{generation.id}</td>
              <td>{generation.status}</td>
              <td>{generation.approved ? 'Yes' : 'No'}</td>
              <td>{new Date(generation.createdAt).toLocaleString()}</td>
              <td onClick={(e) => e.stopPropagation()}>
                {generation.approved ? (
                  <button 
                    className={commonStyles.unapproveButton}
                    onClick={() => onDisapprove(generation.id)}
                    disabled={isDisapprovePending}
                  >
                    {isDisapprovePending ? 'Disapproving...' : 'Disapprove'}
                  </button>
                ) : (
                  <button 
                    className={commonStyles.approveButton}
                    onClick={() => onApprove(generation.id)}
                    disabled={isApprovePending}
                  >
                    {isApprovePending ? 'Approving...' : 'Approve'}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
