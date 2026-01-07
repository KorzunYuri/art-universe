import { useState, useEffect } from 'react';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker/MasterEntityPicker.tsx';
import { CategoryWeightItem } from '../CategoryWeightItem/CategoryWeightItem.tsx';
import type { LookupEntity } from '@/music/shared/types/lookup.ts';
import type { CategoryWeight, GenerationStepUI, StepType } from '@/music/quiz/types/generation-steps.ts';
import styles from './StepBuilder.module.scss';

interface StepBuilderProps {
    stepType: StepType;
    existingStep?: GenerationStepUI | null;
    onStepCreated?: (step: GenerationStepUI) => void;
    onStepUpdate?: (step: GenerationStepUI) => void;
    onStepRemove?: () => void;
    onCancel?: () => void;
    isEditing?: boolean;
    isInline?: boolean;
    readonly?: boolean;
}

export const StepBuilder = ({ 
    stepType, 
    existingStep, 
    onStepCreated, 
    onStepUpdate,
    onStepRemove,
    onCancel, 
    isEditing = false,
    isInline = false,
    readonly = false
}: StepBuilderProps) => {
    const [categories, setCategories] = useState<CategoryWeight[]>([]);
    const [categoryIds, setCategoryIds] = useState<number[]>([]);
    const [targetCount, setTargetCount] = useState(10);

    // Initialize from existing step
    useEffect(() => {
        if (existingStep) {
            setCategories(existingStep.categories || []);
            setCategoryIds(existingStep.categoryIds || []);
            setTargetCount(existingStep.targetCount || 10);
        }
    }, [existingStep]);

    // Auto-update step when data changes (for inline mode)
    useEffect(() => {
        if (isInline && existingStep && onStepUpdate && !readonly) {
            const updatedStep: GenerationStepUI = {
                ...existingStep,
                ...(stepType === 'BLACKLIST_FILTER' 
                    ? { categoryIds: [...categoryIds] }
                    : { categories: [...categories] }
                ),
                ...(stepType === 'FINAL_SELECTION' || stepType === 'FINAL_CATEGORIES_BALANCER' 
                    ? { targetCount } 
                    : {}
                )
            };
            
            // Only update if there are actual changes
            const hasChanges = 
                (stepType === 'BLACKLIST_FILTER' && 
                 JSON.stringify(existingStep.categoryIds) !== JSON.stringify(categoryIds)) ||
                (stepType !== 'BLACKLIST_FILTER' && 
                 JSON.stringify(existingStep.categories) !== JSON.stringify(categories)) ||
                ((stepType === 'FINAL_SELECTION' || stepType === 'FINAL_CATEGORIES_BALANCER') && 
                 existingStep.targetCount !== targetCount);
            
            if (hasChanges) {
                onStepUpdate(updatedStep);
            }
        }
    }, [categories, categoryIds, targetCount, isInline, existingStep, onStepUpdate, stepType, readonly]);

    const handleCategoryAdd = (entity: LookupEntity) => {
        if (stepType === 'BLACKLIST_FILTER') {
            if (categoryIds.includes(entity.id)) return;
            setCategoryIds(prev => [...prev, entity.id]);
        } else {
            if (categories.some(cat => cat.id === entity.id)) return;
            const newCategory: CategoryWeight = {
                id: entity.id,
                name: entity.name,
                weight: 0.5
            };
            setCategories(prev => [...prev, newCategory]);
        }
    };

    const handleWeightChange = (id: number, weight: number) => {
        setCategories(prev => 
            prev.map(cat => cat.id === id ? { ...cat, weight } : cat)
        );
    };

    const handleCategoryRemove = (id: number) => {
        if (stepType === 'BLACKLIST_FILTER') {
            setCategoryIds(prev => prev.filter(catId => catId !== id));
        } else {
            setCategories(prev => prev.filter(cat => cat.id !== id));
        }
    };

    const handleCreateStep = () => {
        if (!onStepCreated) return;
        
        const step: GenerationStepUI = {
            id: existingStep?.id || `${stepType.toLowerCase()}-${Date.now()}`,
            type: stepType,
            ...(stepType === 'BLACKLIST_FILTER' 
                ? { categoryIds: [...categoryIds] }
                : { categories: [...categories] }
            ),
            ...(stepType === 'FINAL_SELECTION' || stepType === 'FINAL_CATEGORIES_BALANCER' 
                ? { targetCount } 
                : {}
            )
        };

        onStepCreated(step);
    };

    const getStepTitle = () => {
        switch (stepType) {
            case 'BLACKLIST_FILTER': return 'Blacklist Filter';
            case 'WHITELIST_FILTER': return 'Whitelist Filter';
            case 'FINAL_SELECTION': return 'Final Selection';
            case 'FINAL_CATEGORIES_BALANCER': return 'Final Categories Balancer';
            default: return 'Step';
        }
    };

    return (
        <div className={`${styles.builder} ${isInline ? styles.inline : ''} ${readonly ? styles.readonly : ''}`}>
            <div className={styles.header}>
                <h4>{getStepTitle()}</h4>
                {!readonly && isInline && onStepRemove && (
                    <button className={styles.removeButton} onClick={onStepRemove}>×</button>
                )}
                {!readonly && !isInline && onCancel && (
                    <button className={styles.cancelButton} onClick={onCancel}>×</button>
                )}
            </div>

            <div className={styles.content}>
                {!readonly && (stepType !== 'FINAL_SELECTION') && (
                    <div className={styles.picker}>
                        <MasterEntityPicker
                            entityType="category"
                            buttonLabel="Add"
                            onEntitySelected={handleCategoryAdd}
                        />
                    </div>
                )}

                {(stepType === 'FINAL_SELECTION' || stepType === 'FINAL_CATEGORIES_BALANCER') && (
                    <div className={styles.targetCountSection}>
                        <label>
                            Target Count:
                            <input 
                                type="number" 
                                value={targetCount} 
                                onChange={(e) => setTargetCount(Number(e.target.value))}
                                min="1"
                                disabled={readonly}
                            />
                        </label>
                    </div>
                )}

                {stepType === 'BLACKLIST_FILTER' && categoryIds.length > 0 && (
                    <div className={styles.categoriesList}>
                        {categoryIds.map(id => (
                            <div key={id} className={styles.categoryItem}>
                                <span>Category ID: {id}</span>
                                {!readonly && (
                                    <button
                                        className={styles.removeButton}
                                        onClick={() => handleCategoryRemove(id)}
                                    >
                                        ×
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}

                {(stepType === 'WHITELIST_FILTER' || stepType === 'FINAL_CATEGORIES_BALANCER') && categories.length > 0 && (
                    <div className={styles.categoriesList}>
                        {categories.map(category => (
                            <CategoryWeightItem
                                key={category.id}
                                category={category}
                                onWeightChange={handleWeightChange}
                                onRemove={handleCategoryRemove}
                                readonly={readonly}
                            />
                        ))}
                    </div>
                )}

                {!readonly && !isInline && (
                    <div className={styles.actions}>
                        <button
                            className={styles.createButton}
                            onClick={handleCreateStep}
                        >
                            {isEditing ? 'Update Step' : 'Create Step'}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};
