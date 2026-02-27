import { LastfmArtistsTable } from "@/music/data/raw/lastfm/components";
import styles from "./LastfmArtists.module.css"

export function LastfmArtists() {
    return (
        <div className={styles.page}>
            <h2>Artists Page</h2>
            <LastfmArtistsTable/>
        </div>
    );
}
