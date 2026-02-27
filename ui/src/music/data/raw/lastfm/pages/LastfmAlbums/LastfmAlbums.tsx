import { LastfmAlbumsTable } from "@/music/data/raw/lastfm/components";
import styles from './LastfmAlbums.module.css';

export function LastfmAlbums() {
    return (
        <div className={styles.page}>
            <h2>Albums Page</h2>
            <LastfmAlbumsTable />
        </div>
    );
}
