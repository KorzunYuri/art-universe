import { DimensionsTable } from "@/music-universe/music-data/components/DimensionsTable";
import styles from './Dimensions.module.css';

export function Dimensions() {
    return (
        <div className={styles.page}>
            <h2>Dimensions</h2>
            <DimensionsTable />
        </div>
    );
}
