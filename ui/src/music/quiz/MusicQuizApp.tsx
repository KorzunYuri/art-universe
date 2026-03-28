import { useRoutes, Navigate } from 'react-router-dom';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { Games } from './components/Games.tsx';
import { GameDetails } from '@/music/quiz/components/GameDetails/GameDetails.tsx';

export default function MusicQuizApp() {
  const permissions = usePermissions();

  if (permissions.quizAccess === 'hidden') {
    return <Navigate to="/" replace />;
  }

  const routes = [
    {
      path: '/',
      element: <Games />,
    },
    {
      path: '/games/:gameId',
      element: <GameDetails />,
    },
  ];

  return useRoutes(routes);
}
