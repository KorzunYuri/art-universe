import { useRoutes } from 'react-router-dom'
import { Categories } from './pages/Categories'
import { Dimensions } from './pages/Dimensions'
import { MusicDataHome } from './pages/MusicDataHome'

export default function MusicDataApp() {
    const routes = [
        { path: '/',            element: <MusicDataHome /> },
        { path: 'categories',   element: <Categories /> },
        { path: 'dimensions',   element: <Dimensions /> },
    ]

    return useRoutes(routes)
}
