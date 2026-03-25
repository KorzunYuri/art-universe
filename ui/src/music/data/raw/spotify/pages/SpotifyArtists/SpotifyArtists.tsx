import { SpotifyArtistsTable } from "@/music/data/raw/spotify/components/SpotifyArtistsTable/SpotifyArtistsTable";
import styles from "./SpotifyArtists.module.css";

export function SpotifyArtists() {
    return (
        <div className={styles.page}>
            <h2>Artists Page</h2>
            <SpotifyArtistsTable />
        </div>
    );
}
