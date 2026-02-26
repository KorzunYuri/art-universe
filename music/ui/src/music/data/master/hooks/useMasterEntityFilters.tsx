import { useState } from 'react';
import { useAdditionalSearchFields } from '@/shared/hooks/useAdditionalSearchFields.ts';
import { EntityLookup } from '@/shared/components';
import { LookupContextFactory } from '@/shared/types/lookup-context.ts';
import type { LookupEntity } from '@/shared/types/lookup.ts';

/**
 * Hook for managing category filter for master entities
 */
export function useCategoryFilter() {
    const [selectedCategory, setSelectedCategory] = useState<LookupEntity | null>(null);
    const [categorySearchString, setCategorySearchString] = useState('');
    
    const { createCustomField } = useAdditionalSearchFields();
    
    const categoryField = createCustomField(
        'category',
        'Category',
        () => (
            <div>
                <label style={{ 
                    display: 'block', 
                    marginBottom: '0.25rem', 
                    fontSize: '0.875rem', 
                    fontWeight: '500',
                    color: '#495057'
                }}>
                    Category:
                </label>
                <EntityLookup
                    dataSource="master"
                    entityType="category"
                    searchString={categorySearchString}
                    context={LookupContextFactory.basic()}
                    onSelect={setSelectedCategory}
                    onChange={setCategorySearchString}
                    selectedEntity={selectedCategory}
                    placeholder="Search for category..."
                    autoSelectExactMatch={true}
                    limit={10}
                />
            </div>
        )
    );
    
    return {
        categoryId: selectedCategory?.id,
        selectedCategory,
        setSelectedCategory,
        categorySearchString,
        setCategorySearchString,
        categoryField
    };
}
