import { NavigationCard } from '@/shared/components';
import styles from './ArtDataHome.module.css';

export function ArtDataHome() {
    return (
        <div className={styles.container}>
            <h2>Art Data</h2>
            <NavigationCard to="/art/data/master/persons" label="Persons" />
        </div>
    );
}
