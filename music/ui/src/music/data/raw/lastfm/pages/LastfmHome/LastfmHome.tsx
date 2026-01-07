import styles from './LastfmHome.module.css';
import { NavigationCard } from "@/music/shared/components";
import { MaintenanceButton } from "@/music/data/raw/lastfm/components/MaintenanceButton/MaintenanceButton.tsx";

export function LastfmHome() {
    return (
        <div className={styles.container}>
            <h2>Last.fm</h2>
            <NavigationCard to="/music/data/raw/lastfm/tags"       label="Tags" />
            <NavigationCard to="/music/data/raw/lastfm/artists"    label="Artists" />
            <NavigationCard to="/music/data/raw/lastfm/albums"     label="Albums" />
            <NavigationCard to="/music/data/raw/lastfm/tracks"     label="Tracks" />
            <MaintenanceButton />
        </div>
    )
}