import { AlbumsTable } from "@/music-universe/music-data/components/AlbumsTable";
import styles from './Albums.module.css';

export function Albums() {
    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Albums</h2>
            </div>
            
            <AlbumsTable />
        </div>
    );
}
