import { useRoutes } from 'react-router-dom'
import { NavigationCard } from '@/shared/components'
import { QueryProvider } from '@/shared/providers/QueryProvider'
import { AuthProvider } from '@/shared/providers/AuthProvider'
import { NotificationProvider } from '@/shared/providers/NotificationProvider'
import { NotificationContainer } from '@/shared/components/NotificationContainer'
import { AppHeader } from '@/shared/components/AppHeader'
import LastfmApp from '@/music/data/raw/lastfm/LastfmApp.tsx'
import MusicMasterApp from '@/music/data/master/MusicMasterApp.tsx'
import ArtMasterApp from '@/art/data/master/ArtMasterApp.tsx'
import MusicQuizApp from '@/music/quiz/MusicQuizApp.tsx'
import styles from './App.module.css'

const routes = [
    {
        path: '/',
        element: (
            <div>
                <h2>Choose data source:</h2>
                <NavigationCard to="/art/data/master" label="Art Master" />
                <NavigationCard to="/music/data/master" label="Music Master" />
                <NavigationCard to="/music/data/raw/lastfm" label="Last.fm" />
                <NavigationCard to="/music/quiz" label="Music Quiz" />
            </div>
        ),
    },
    {
        path: '/art/data/master/*',
        element: <ArtMasterApp />,
    },
    {
        path: '/music/data/master/*',
        element: <MusicMasterApp />,
    },
    {
        path: '/music/data/raw/lastfm/*',
        element: <LastfmApp />,
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

export default function App() {
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
