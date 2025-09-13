import { useState } from 'react';
import { MasterEntityPicker } from '@/music-universe/music-data/components/MasterEntityPicker/MasterEntityPicker';
import { CategoryWeightItem } from '../CategoryWeightItem/CategoryWeightItem';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type { CategoryWeight, GenerationStepUI } from '../../types/generation-steps';
import styles from './WhitelistStepBuilder.module.scss';

interface WhitelistStepBuilderProps {
    onStepCreated: (step: GenerationStepUI) => void;
    onCancel: () => void;
}

export const WhitelistStepBuilder = ({ onStepCreated, onCancel }: WhitelistStepBuilderProps) => {
    const [categories, setCategories] = useState<CategoryWeight[]>([]);

    const handleCategoryAdd = (entity: LookupEntity) => {
        // Check if category already exists
        if (categories.some(cat => cat.id === entity.id)) {
            return;
        }

        const newCategory: CategoryWeight = {
            id: entity.id,
            name: entity.name,
            weight: 0.5 // default weight
        };

        setCategories(prev => [...prev, newCategory]);
    };

    const handleWeightChange = (id: number, weight: number) => {
        setCategories(prev => 
            prev.map(cat => cat.id === id ? { ...cat, weight } : cat)
        );
    };

    const handleCategoryRemove = (id: number) => {
        setCategories(prev => prev.filter(cat => cat.id !== id));
    };

    const handleCreateStep = () => {
        const step: GenerationStepUI = {
            id: `whitelist-${Date.now()}`, // temporary ID
            type: 'WHITELIST_FILTER',
            categories: [...categories]
        };

        onStepCreated(step);
    };

    const canCreateStep = categories.length >= 2;

    return (
        <div className={styles.builder}>
            <div className={styles.header}>
                <h4>Create Whitelist Step</h4>
                <button className={styles.cancelButton} onClick={onCancel}>×</button>
            </div>

            <div className={styles.picker}>
                <MasterEntityPicker
                    entityType="category"
                    buttonLabel="Add"
                    onEntitySelected={handleCategoryAdd}
                />
                <button
                    className={styles.createButton}
                    onClick={handleCreateStep}
                    disabled={!canCreateStep}
                >
                    Create Step
                </button>
            </div>

            {categories.length > 0 && (
                <div className={styles.categoriesList}>
                    {categories.map(category => (
                        <CategoryWeightItem
                            key={category.id}
                            category={category}
                            onWeightChange={handleWeightChange}
                            onRemove={handleCategoryRemove}
                        />
                    ))}
                </div>
            )}

            {categories.length > 0 && categories.length < 2 && (
                <div className={styles.hint}>
                    Add at least 2 categories to create a step
                </div>
            )}
        </div>
    );
};
