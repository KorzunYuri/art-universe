import { LastfmTracksTable } from "@/music/data/raw/lastfm/components/LastfmTracksTable/LastfmTracksTable.tsx";
import styles from "./LastfmTracks.module.css";

export function LastfmTracks() {
    return (
        <div className={styles.page}>
            <h2>Tracks Page</h2>
            <LastfmTracksTable />
        </div>
    );
}
