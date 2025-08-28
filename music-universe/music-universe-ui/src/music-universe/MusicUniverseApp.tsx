import { useRoutes } from 'react-router-dom'
import { NavigationCard } from './shared/components'
import LastfmApp from './sources/lastfm/LastfmApp'
import MusicDataApp from './music-data/MusicDataApp'
import MusicQuizApp from '../music-quiz/MusicQuizApp'

export default function MusicUniverseApp() {
    const routes = [
        {
            path: '/',
            element: (
                <div>
                    <h1>Choose data source:</h1>
                    <NavigationCard to="/lastfm" label="Last.fm" />
                    <NavigationCard to="/music-data" label="Music Data" />
                    <NavigationCard to="/music-quiz" label="Music Quiz" />
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
        {
            path: '/music-quiz/*',  // "*" allows routing within MusicQuizApp
            element: <MusicQuizApp />,
        },
    ]

    return useRoutes(routes)
}