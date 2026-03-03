import { PersonsTable } from '@/art/data/master/components/PersonsTable';
import styles from './Persons.module.css';

export function Persons() {
    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Persons</h2>
            </div>

            <PersonsTable />
        </div>
    );
}
