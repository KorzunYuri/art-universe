import { useRoutes } from 'react-router-dom'
import { NavigationCard } from './shared/components'
import LastfmApp from './sources/lastfm/LastfmApp'
import MusicDataApp from './music-data/MusicDataApp'

export default function MusicUniverseApp() {
    const routes = [
        {
            path: '/',
            element: (
                <div>
                    <h1>Choose data source:</h1>
                    <NavigationCard to="/lastfm" label="Last.fm" />
                    <NavigationCard to="/music-data" label="Music Data" />
                </div>
            ),
        },
        {
            path: '/lastfm/*',  // "*" allows routing within LastfmApp
            element: <LastfmApp />,
        },
        {
            path: '/music-data/*',  // "*" allows routing within MusicDataApp
            element: <MusicDataApp />,
        },
    ]

    return useRoutes(routes)
}