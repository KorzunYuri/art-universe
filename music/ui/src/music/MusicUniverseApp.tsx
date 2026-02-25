import { useRoutes } from 'react-router-dom'
import { NavigationCard } from './shared/components'
import { QueryProvider } from './shared/providers/QueryProvider'
import { AuthProvider } from './shared/providers/AuthProvider'
import { NotificationProvider } from './shared/providers/NotificationProvider'
import { NotificationContainer } from './shared/components/NotificationContainer'
import { AppHeader } from './shared/components/AppHeader'
import LastfmApp from './data/raw/lastfm/LastfmApp.tsx'
import MasterDataApp from './data/master/MasterDataApp.tsx'
import MusicQuizApp from './quiz/MusicQuizApp.tsx'
import styles from './MusicUniverseApp.module.css'

const routes = [
    {
        path: '/',
        element: (
            <div>
                <h2>Choose data source:</h2>
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

function AppContent() {
    const content = useRoutes(routes)
    return (
        <div className={styles.layout}>
            <AppHeader />
            <main className={styles.content}>
                {content}
            </main>
        </div>
    )
}

export default function MusicUniverseApp() {
    return (
        <QueryProvider>
            <AuthProvider>
                <NotificationProvider>
                    <AppContent />
                    <NotificationContainer />
                </NotificationProvider>
            </AuthProvider>
        </QueryProvider>
    )
}
