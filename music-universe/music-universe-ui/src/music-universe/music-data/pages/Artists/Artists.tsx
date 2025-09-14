import { ArtistsTable } from "@/music-universe/music-data/components/ArtistsTable";
import styles from './Artists.module.css';

export function Artists() {
    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Artists</h2>
            </div>
            
            <ArtistsTable />
        </div>
    );
}
