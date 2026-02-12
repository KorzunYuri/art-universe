import { useRoutes } from 'react-router-dom'
import { NavigationCard } from './shared/components'
import { QueryProvider } from './shared/providers/QueryProvider'
import { AuthProvider } from './shared/providers/AuthProvider'
import { NotificationProvider } from './shared/providers/NotificationProvider'
import { NotificationContainer } from './shared/components/NotificationContainer'
import LastfmApp from './data/raw/lastfm/LastfmApp.tsx'
import MasterDataApp from './data/master/MasterDataApp.tsx'
import MusicQuizApp from './quiz/MusicQuizApp.tsx'

export default function MusicUniverseApp() {
    const routes = [
        {
            path: '/',
            element: (
                <div>
                    <h1>Choose data source:</h1>
                    <NavigationCard to="/music/data/raw/lastfm" label="Last.fm" />
                    <NavigationCard to="/music/data/master" label="Music Data" />
                    <NavigationCard to="/music/quiz" label="Music Quiz" />
                </div>
            ),
        },
        {
            path: '/music/data/raw/lastfm/*',
            element: <LastfmApp />,
        },
        {
            path: '/music/data/master/*',
            element: <MasterDataApp />,
        },
        {
            path: '/music/quiz/*',
            element: <MusicQuizApp />,
        },
    ]

    return (
        <QueryProvider>
            <AuthProvider>
                <NotificationProvider>
                    {useRoutes(routes)}
                    <NotificationContainer />
                </NotificationProvider>
            </AuthProvider>
        </QueryProvider>
    )
}