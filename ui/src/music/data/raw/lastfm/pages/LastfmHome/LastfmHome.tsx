import styles from './LastfmHome.module.css';
import { NavigationCard, AccessGate } from "@/shared/components";
import { usePermissions } from "@/shared/hooks/usePermissions";

export function LastfmHome() {
    const permissions = usePermissions();

    return (
        <div className={styles.container}>
            <h2>Last.fm</h2>
            <NavigationCard to="/music/data/raw/lastfm/tags"       label="Tags" />
            <NavigationCard to="/music/data/raw/lastfm/artists"    label="Artists" />
            <NavigationCard to="/music/data/raw/lastfm/albums"     label="Albums" />
            <NavigationCard to="/music/data/raw/lastfm/tracks"     label="Tracks" />
            <AccessGate level={permissions.adminAccess}>
                <NavigationCard to="/music/data/raw/lastfm/admin"  label="Administration" />
            </AccessGate>
        </div>
    )
}