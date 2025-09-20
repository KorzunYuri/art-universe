import { useState } from 'react';
import { useMasterEntityTable } from "@/music-universe/music-data/hooks/useMasterEntityTable";
import { useNotifications } from '@/music-universe/shared/hooks';
import { useCategoryFilter } from "@/music-universe/music-data/hooks/useMasterEntityFilters";
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { ArtistsTableRow } from "@/music-universe/music-data/components/ArtistsTableRow";
import { createArtist } from '@/music-universe/music-data/api/music-data-artists';
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
import styles from './ArtistsTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'categories', label: 'Categories', className: styles.categories },
    { key: 'addCategory', label: 'Add Category', className: styles.addCategory },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const ArtistsTable = () => {
    const { showNotification } = useNotifications();
    const [newArtistName, setNewArtistName] = useState('');
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
        handleSearchSubmit: submitSearch,
        updateParams,
        refresh
    } = useMasterEntityTable("artist");

    // Filter hooks
    const { categoryId, categoryField } = useCategoryFilter();

    const handleSearchSubmit = () => {
        submitSearch();
        updateParams({
            categoryId: categoryId || undefined
        });
    };

    const handleCreateArtist = async () => {
        if (!newArtistName.trim() || isCreating) return;

        setIsCreating(true);
        try {
            const created = await createArtist({ name: newArtistName.trim() });
            if (created) {
                console.log('✅ Artist created successfully:', created.id);
                setNewArtistName('');
                refresh();
            }
        } catch (error: any) {
            console.error('❌ Error creating artist:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create artist');
        } finally {
            setIsCreating(false);
        }
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            categoryField
        ]
    };

    return (
        <div>
            <div className={styles.createSection}>
                <input
                    type="text"
                    value={newArtistName}
                    onChange={(e) => setNewArtistName(e.target.value)}
                    placeholder="New artist name"
                    disabled={isCreating}
                    onKeyDown={(e) => e.key === 'Enter' && handleCreateArtist()}
                />
                <button
                    onClick={handleCreateArtist}
                    disabled={!newArtistName.trim() || isCreating}
                    className={styles.createButton}
                >
                    {isCreating ? '...' : 'Create'}
                </button>
            </div>
            
            <EntityTable
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={handleSearchSubmit}
                searchPlaceholder="Search artist name..."
                
                additionalSearch={additionalSearchConfig}
                
                columns={columns}
                emptyMessage="No artists found"
                
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
                    <ArtistsTableRow
                        key={entityId}
                        entityId={entityId}
                        onDeleted={refresh}
                    />
                ))}
            </EntityTable>
        </div>
    );
};
