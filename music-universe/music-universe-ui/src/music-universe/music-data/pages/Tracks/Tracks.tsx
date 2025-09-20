import { TracksTable } from "@/music-universe/music-data/components/TracksTable";
import styles from './Tracks.module.css';

export function Tracks() {
    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Tracks</h2>
            </div>
            
            <TracksTable />
        </div>
    );
}
