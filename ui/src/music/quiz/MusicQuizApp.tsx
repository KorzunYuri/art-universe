import { useRoutes } from 'react-router-dom';
import { Games } from './components/Games.tsx';
import { GameDetails } from '@/music/quiz/components/GameDetails/GameDetails.tsx';

export default function MusicQuizApp() {
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
