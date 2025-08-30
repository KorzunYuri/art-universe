import { useRoutes } from 'react-router-dom';
import { QueryProvider } from '@/music-universe/shared/providers/QueryProvider.tsx';
import { Games } from './components/Games.tsx';
import { GameDetails } from './components/GameDetails.tsx';


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
