import { useRoutes } from 'react-router-dom'
import { NavigationCard } from './shared/components'
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
            path: '/lastfm/*',  // "*" allows routing within LastfmApp
            element: <LastfmApp />,
        },
    ]

    return useRoutes(routes)
}