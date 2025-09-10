import { useState } from 'react';
import { CategoriesTable } from "@/music-universe/music-data/components/CategoriesTable";
import { CategoriesDag } from "@/music-universe/music-data/components/CategoryDag";
import styles from './Categories.module.css';

type ViewMode = 'table' | 'graph';

export function Categories() {
    const [viewMode, setViewMode] = useState<ViewMode>('table');

    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Categories</h2>
                <div className={styles.viewToggle}>
                    <button 
                        className={`${styles.toggleButton} ${viewMode === 'table' ? styles.active : ''}`}
                        onClick={() => setViewMode('table')}
                    >
                        Table
                    </button>
                    <button 
                        className={`${styles.toggleButton} ${viewMode === 'graph' ? styles.active : ''}`}
                        onClick={() => setViewMode('graph')}
                    >
                        Graph
                    </button>
                </div>
            </div>
            
            {viewMode === 'table' ? <CategoriesTable /> : <CategoriesDag />}
        </div>
    );
}
