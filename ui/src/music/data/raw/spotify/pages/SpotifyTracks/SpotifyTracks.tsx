import { SpotifyTracksTable } from "@/music/data/raw/spotify/components/SpotifyTracksTable/SpotifyTracksTable";
import styles from "./SpotifyTracks.module.css";

export function SpotifyTracks() {
    return (
        <div className={styles.page}>
            <h2>Tracks Page</h2>
            <SpotifyTracksTable />
        </div>
    );
}
