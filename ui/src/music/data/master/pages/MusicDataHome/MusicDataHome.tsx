import { NavigationCard } from "@/shared/components";
import styles from './MusicDataHome.module.css';

export function MusicDataHome() {
    return (
        <div className={styles.container}>
            <h2>Music Master</h2>
            <NavigationCard to="/music/data/master/categories" label="Categories" />
            <NavigationCard to="/music/data/master/artists" label="Artists" />
            <NavigationCard to="/music/data/master/albums" label="Albums" />
            <NavigationCard to="/music/data/master/tracks" label="Tracks" />
            <NavigationCard to="/music/data/master/relation-types" label="Relation Types" />
        </div>
    )
}
