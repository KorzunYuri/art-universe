import { useRoutes } from 'react-router-dom';
import { QueryProvider } from '@/music/shared/providers/QueryProvider.tsx';
import { NotificationProvider } from '@/music/shared/providers/NotificationProvider.tsx';
import { NotificationContainer } from '@/music/shared/components/NotificationContainer/NotificationContainer.tsx';
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

  return (
    <QueryProvider>
      <NotificationProvider>
        {useRoutes(routes)}
        <NotificationContainer />
      </NotificationProvider>
    </QueryProvider>
  );
}
