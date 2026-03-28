import { useRoutes } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import { NavigationCard, AccessGate } from '@/shared/components'
import { QueryProvider } from '@/shared/providers/QueryProvider'
import { AuthProvider } from '@/shared/providers/AuthProvider'
import { NotificationProvider } from '@/shared/providers/NotificationProvider'
import { NotificationContainer } from '@/shared/components/NotificationContainer'
import { AppHeader } from '@/shared/components/AppHeader'
import { LoginPage } from '@/shared/components/LoginPage'
import { useAuth } from '@/shared/hooks/useAuth'
import { usePermissions } from '@/shared/hooks/usePermissions'
import { appConfig } from '@/shared/config/appConfig'
import LastfmApp from '@/music/data/raw/lastfm/LastfmApp.tsx'
import SpotifyApp from '@/music/data/raw/spotify/SpotifyApp.tsx'
import MusicMasterApp from '@/music/data/master/MusicMasterApp.tsx'
import ArtMasterApp from '@/art/data/master/ArtMasterApp.tsx'
import MusicQuizApp from '@/music/quiz/MusicQuizApp.tsx'
import ConfigApp from '@/common/config/ConfigApp.tsx'
import styles from './App.module.css'

function HomePage() {
    const permissions = usePermissions()
    return (
        <div>
            <h2>Choose data source:</h2>
            <NavigationCard to="/art/data/master" label="Art Master" />
            <NavigationCard to="/music/data/master" label="Music Master" />
            <NavigationCard to="/music/data/raw/lastfm" label="Last.fm" />
            <NavigationCard to="/music/data/raw/spotify" label="Spotify" />
            <AccessGate level={permissions.quizAccess}>
                <NavigationCard to="/music/quiz" label="Music Quiz" />
            </AccessGate>
            <AccessGate level={permissions.configAccess}>
                <NavigationCard to="/common/config" label="Configuration" />
            </AccessGate>
        </div>
    )
}

const routes = [
    {
        path: '/',
        element: <HomePage />,
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
        path: '/music/data/raw/spotify/*',
        element: <SpotifyApp />,
    },
    {
        path: '/music/quiz/*',
        element: <MusicQuizApp />,
    },
    {
        path: '/common/config/*',
        element: <ConfigApp />,
    },
]

function AuthenticatedApp() {
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

function AppContent() {
    const { isAuthenticated, isLoading } = useAuth()

    if (isLoading) {
        return <div className={styles.loading}>Loading...</div>
    }

    if (!isAuthenticated) {
        return <LoginPage />
    }

    return <AuthenticatedApp />
}

export default function App() {
    return (
        <GoogleOAuthProvider clientId={appConfig.googleClientId}>
            <QueryProvider>
                <AuthProvider>
                    <NotificationProvider>
                        <AppContent />
                        <NotificationContainer />
                    </NotificationProvider>
                </AuthProvider>
            </QueryProvider>
        </GoogleOAuthProvider>
    )
}
