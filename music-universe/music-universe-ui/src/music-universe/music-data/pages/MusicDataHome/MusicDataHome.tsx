import { NavigationCard } from "@/music-universe/shared/components";
import styles from './MusicDataHome.module.css';

export function MusicDataHome() {
    return (
        <div className={styles.container}>
            <h2>Music Data</h2>
            <NavigationCard to="/music-data/categories" label="Categories" />
            <NavigationCard to="/music-data/artists" label="Artists" />
            <NavigationCard to="/music-data/albums" label="Albums" />
            <NavigationCard to="/music-data/tracks" label="Tracks" />
        </div>
    )
}
