import { LastfmTracksTable } from "@/music-universe/sources/lastfm/components/LastfmTracksTable/LastfmTracksTable";
import styles from "./LastfmTracks.module.css";

export function LastfmTracks() {
    return (
        <div className={styles.page}>
            <h2>Tracks Page</h2>
            <LastfmTracksTable />
        </div>
    );
}
