import { useNavigate } from 'react-router-dom';
import { useGames, useCreateGame } from '../hooks/useQuizData';
import styles from '../MusicQuizApp.module.css';

export const Games = () => {
  const navigate = useNavigate();
  const { data: games, isLoading } = useGames();
  const createGameMutation = useCreateGame();

  const handleCreateGame = () => {
    createGameMutation.mutate();
  };

  if (isLoading) return <div>Loading...</div>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.title}>Games</h1>
        <button className={styles.button} onClick={handleCreateGame} disabled={createGameMutation.isPending}>
          {createGameMutation.isPending ? 'Creating...' : 'Create Game'}
        </button>
      </div>
      
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Generation ID</th>
            <th>Created At</th>
          </tr>
        </thead>
        <tbody>
          {games?.content.map((game) => (
            <tr key={game.id} onClick={() => navigate(`/music-quiz/games/${game.id}`)}>
              <td>{game.id}</td>
              <td>{game.generationId || 'None'}</td>
              <td>{new Date(game.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
