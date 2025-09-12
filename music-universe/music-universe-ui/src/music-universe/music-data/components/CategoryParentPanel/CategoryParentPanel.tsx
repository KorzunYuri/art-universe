import { useState } from 'react';
import { CategoryParentItem } from '../CategoryParentItem/CategoryParentItem';
import { deleteCategoryRelation } from '@/music-universe/music-data/api/music-data-categories';
import type { CategoryDto } from '@/music-universe/music-data/api/music-data-categories';
import styles from './CategoryParentPanel.module.scss';

export interface CategoryParentPanelProps {
    categoryId: number;
    parents: CategoryDto[];
    onParentRemoved?: () => void;
}

export const CategoryParentPanel = ({
    categoryId,
    parents,
    onParentRemoved
}: CategoryParentPanelProps) => {
    const [processingParents, setProcessingParents] = useState<Set<number>>(new Set());

    const handleRemoveParent = async (parentId: number) => {
        setProcessingParents(prev => new Set(prev).add(parentId));
        
        try {
            await deleteCategoryRelation({
                sourceId: parentId,
                targetId: categoryId
            });
            
            console.log(`✅ Successfully removed parent ${parentId} from category ${categoryId}`);
            onParentRemoved?.();
        } catch (error) {
            console.error(`❌ Error removing parent ${parentId}:`, error);
        } finally {
            setProcessingParents(prev => {
                const newSet = new Set(prev);
                newSet.delete(parentId);
                return newSet;
            });
        }
    };

    if (parents.length === 0) {
        return (
            <div className={styles.emptyPanel}>
                No parents
            </div>
        );
    }

    return (
        <div className={styles.parentPanel}>
            <div className={styles.parentList}>
                {parents.map(parent => (
                    <CategoryParentItem
                        key={parent.id}
                        id={parent.id}
                        name={parent.name}
                        onRemove={handleRemoveParent}
                        isProcessing={processingParents.has(parent.id)}
                    />
                ))}
            </div>
        </div>
    );
};
