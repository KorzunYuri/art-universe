import { useRoutes } from 'react-router-dom';
import { QueryProvider } from '@/music-universe/shared/providers/QueryProvider';
import { Games } from './components/Games';
import { GameDetails } from './components/GameDetails';


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

  return (
    <QueryProvider>
      {useRoutes(routes)}
    </QueryProvider>
  );
}
