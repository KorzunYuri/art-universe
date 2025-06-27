import { CategoriesTable } from "@/music-universe/music-data/components/CategoriesTable";
import styles from './Categories.module.css';

export function Categories() {
    return (
        <div className={styles.page}>
            <h2>Categories</h2>
            <CategoriesTable />
        </div>
    );
}
