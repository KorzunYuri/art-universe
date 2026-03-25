import { SpotifyGenresTable } from "@/music/data/raw/spotify/components/SpotifyGenresTable/SpotifyGenresTable";
import styles from "./SpotifyGenres.module.css";

export function SpotifyGenres() {
    return (
        <div className={styles.page}>
            <h2>Genres Page</h2>
            <SpotifyGenresTable />
        </div>
    );
}
