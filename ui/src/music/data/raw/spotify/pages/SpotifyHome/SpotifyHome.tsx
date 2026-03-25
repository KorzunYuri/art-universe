import styles from './SpotifyHome.module.css';
import { NavigationCard } from "@/shared/components";

export function SpotifyHome() {
    return (
        <div className={styles.container}>
            <h2>Spotify</h2>
            <NavigationCard to="/music/data/raw/spotify/artists" label="Artists" />
            <NavigationCard to="/music/data/raw/spotify/albums" label="Albums" />
            <NavigationCard to="/music/data/raw/spotify/tracks" label="Tracks" />
            <NavigationCard to="/music/data/raw/spotify/genres" label="Genres" />
        </div>
    )
}
