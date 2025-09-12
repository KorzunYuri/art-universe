import { useState } from 'react';
import { useNotifications } from '@/music-universe/shared/hooks';
import { EntityLookup } from '@/music-universe/shared/components';
import { createCategoryRelation } from '@/music-universe/music-data/api/music-data-categories';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type { DataSource } from '@/music-universe/sources/shared/types/data-sources';
import type { MasterEntityType } from '@/music-universe/shared/types/entities';
import { LookupContextFactory } from '@/music-universe/shared/types/lookup-context';
import styles from './CategoryParentAdder.module.scss';

export interface CategoryParentAdderProps {
    categoryId: number;
    dataSource: DataSource | 'master';
    entityType: MasterEntityType;
    buttonLabel: string;
    onParentAdded?: () => void;
}

export const CategoryParentAdder = ({
    categoryId,
    dataSource,
    entityType,
    buttonLabel,
    onParentAdded
}: CategoryParentAdderProps) => {
    const { showNotification } = useNotifications();
    const [searchString, setSearchString] = useState('');
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const handleEntitySelect = (entity: LookupEntity | null) => {
        setSelectedEntity(entity);
    };

    const handleSearchChange = (value: string) => {
        setSearchString(value);
        // Clear selection if user types something different
        if (selectedEntity && value !== selectedEntity.name) {
            setSelectedEntity(null);
        }
    };

    const handleAddParent = async () => {
        if (!selectedEntity || isProcessing) return;

        setIsProcessing(true);
        try {
            await createCategoryRelation({
                sourceId: selectedEntity.id,
                targetId: categoryId
            });

            console.log(`✅ Successfully added parent ${selectedEntity.id} to category ${categoryId}`);
            
            // Clear the form
            setSearchString('');
            setSelectedEntity(null);
            
            onParentAdded?.();
        } catch (error: any) {
            console.error(`❌ Error adding parent ${selectedEntity.id}:`, error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add parent');
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <div className={styles.parentAdder}>
            <div className={styles.lookupContainer}>
                <EntityLookup
                    dataSource={dataSource}
                    entityType={entityType}
                    searchString={searchString}
                    context={LookupContextFactory.basic()}
                    onChange={handleSearchChange}
                    onSelect={handleEntitySelect}
                    selectedEntity={selectedEntity}
                    placeholder="Search for parent category"
                    disabled={isProcessing}
                />
            </div>
            <button
                className={styles.addButton}
                onClick={handleAddParent}
                disabled={!selectedEntity || isProcessing}
            >
                {isProcessing ? '...' : buttonLabel}
            </button>
        </div>
    );
};
