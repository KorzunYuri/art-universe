import { RelationTypesTable } from '@/music/data/master/components/RelationTypesTable';
import styles from './RelationTypes.module.css';

export function RelationTypes() {
    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Relation Types</h2>
            </div>

            <RelationTypesTable />
        </div>
    );
}
