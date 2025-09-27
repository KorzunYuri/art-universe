// hooks
import { useState } from 'react';
import { useMasterEntityTable } from "@/music/data/master/hooks/useMasterEntityTable.ts";
import { useNotifications } from '@/music/shared/hooks';
// components
import { EntityTable, type EntityTableColumn } from "@/music/shared/components/EntityTable/EntityTable.tsx";
import { CategoriesTableRow } from "@/music/data/master/components/CategoriesTableRow";
// api
import { createCategory } from '@/music/data/master/api/music-data-categories.ts';
// styles
import styles from './CategoriesTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'parents', label: 'Parents', className: styles.parents },
    { key: 'addParent', label: 'Add Parent', className: styles.addParent },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const CategoriesTable = () => {
    const { showNotification } = useNotifications();
    const [newCategoryName, setNewCategoryName] = useState('');
    const [isCreating, setIsCreating] = useState(false);
    
    const {
        entityIds,
        pagination,
        search,
        sort,
        isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        refresh
    } = useMasterEntityTable("category");

    const handleCreateCategory = async () => {
        if (!newCategoryName.trim() || isCreating) return;

        setIsCreating(true);
        try {
            const created = await createCategory({ name: newCategoryName.trim() });
            if (created) {
                console.log('✅ Category created successfully:', created.id);
                setNewCategoryName('');
                refresh();
            }
        } catch (error: any) {
            console.error('❌ Error creating category:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create category');
        } finally {
            setIsCreating(false);
        }
    };

    return (
        <div>
            <div className={styles.createSection}>
                <input
                    type="text"
                    value={newCategoryName}
                    onChange={(e) => setNewCategoryName(e.target.value)}
                    placeholder="New category name"
                    disabled={isCreating}
                    onKeyDown={(e) => e.key === 'Enter' && handleCreateCategory()}
                />
                <button
                    onClick={handleCreateCategory}
                    disabled={!newCategoryName.trim() || isCreating}
                    className={styles.createButton}
                >
                    {isCreating ? '...' : 'Create'}
                </button>
            </div>
            
            <EntityTable
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={refresh}
                searchPlaceholder="Search category name..."

                columns={columns}
                emptyMessage="No categories found"

                sort={sort}
                onSortChange={setSort}

                pagination={{
                    page: pagination.page,
                    totalPages: pagination.totalPages,
                    hasNextPage: pagination.hasNextPage,
                    hasPrevPage: pagination.hasPrevPage,
                }}
                onNextPage={nextPage}
                onPrevPage={prevPage}
                onGoToPage={goToPage}

                isLoading={isLoading}
                onRefresh={refresh}
            >
            {entityIds?.map(entityId => (
                <CategoriesTableRow
                    key={entityId}
                    entityId={entityId}
                    onDeleted={refresh}
                />
            ))}
        </EntityTable>
        </div>
    );
};
