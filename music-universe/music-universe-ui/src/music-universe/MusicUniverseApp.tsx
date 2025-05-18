import { useRoutes } from 'react-router-dom'
import NavigationCard from './shared/components/NavigationCard'
import LastfmApp from './sources/lastfm/LastfmApp'

export default function MusicUniverseApp() {
    const routes = [
        {
            path: '/',
            element: (
                <div>
                    <h1>Choose data source:</h1>
                    <NavigationCard to="/lastfm" label="Last.fm" />
                </div>
            ),
        },
        {
            path: '/lastfm/*',  // "*" позволяет маршрутизировать внутри LastfmApp
            element: <LastfmApp />,
        },
    ]

    return useRoutes(routes)
}