import styles from './LastfmHome.module.css';
import { NavigationCard } from "@/music-universe/shared/components";
import { MaintenanceButton } from "@/music-universe/sources/lastfm/components/MaintenanceButton/MaintenanceButton";

export function LastfmHome() {
    return (
        <div className={styles.container}>
            <h2>Last.fm</h2>
            <NavigationCard to="/lastfm/tags"       label="Tags" />
            <NavigationCard to="/lastfm/artists"    label="Artists" />
            <NavigationCard to="/lastfm/albums"     label="Albums" />
            <NavigationCard to="/lastfm/tracks"     label="Tracks" />
            <MaintenanceButton />
        </div>
    )
}