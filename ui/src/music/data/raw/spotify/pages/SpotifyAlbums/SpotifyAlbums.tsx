import { SpotifyAlbumsTable } from "@/music/data/raw/spotify/components/SpotifyAlbumsTable/SpotifyAlbumsTable";
import styles from './SpotifyAlbums.module.css';

export function SpotifyAlbums() {
    return (
        <div className={styles.page}>
            <h2>Albums Page</h2>
            <SpotifyAlbumsTable />
        </div>
    );
}
