import {type ReactNode, useState, useEffect, useRef, useCallback } from 'react';
import { Pagination } from '../Pagination';
import { AdditionalSearchFields } from './AdditionalSearchFields';
import type { AdditionalSearchConfig } from './types';
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './EntityTable.module.scss';

export interface EntityTableColumn {
    key: string;
    label: string;
    sortable?: boolean;
    className?: string;
}

export interface EntityTablePagination {
    page: number;
    totalPages: number;
    hasNextPage: boolean;
    hasPrevPage: boolean;
}

export interface EntityTableProps {
    // Search functionality
    search: string;
    onSearchChange: (search: string) => void;
    onSearchSubmit: () => void;
    searchPlaceholder?: string;
    
    // Additional search fields
    additionalSearch?: AdditionalSearchConfig;
    
    // Table data
    columns: EntityTableColumn[];
    children: ReactNode; // Table rows
    emptyMessage?: string;
    
    // Sorting
    sort: string;
    onSortChange: (sort: string) => void;
    
    // Pagination
    pagination: EntityTablePagination;
    onNextPage: () => void;
    onPrevPage: () => void;
    onGoToPage: (page: number) => void;
    
    // Loading state
    isLoading: boolean;
    
    // Actions
    onRefresh: () => void;
    extraActions?: ReactNode;
    
    // Styling
    className?: string;
}

export const EntityTable = ({
    search,
    onSearchChange,
    onSearchSubmit,
    searchPlaceholder = "Search...",
    additionalSearch,
    columns,
    children,
    emptyMessage = "No items found",
    sort,
    onSortChange,
    pagination,
    onNextPage,
    onPrevPage,
    onGoToPage,
    isLoading,
    onRefresh,
    extraActions,
    className = ''
}: EntityTableProps) => {
    
    const [showAdditionalFields, setShowAdditionalFields] = useState(
        additionalSearch?.defaultCollapsed === false
    );

    const debounceTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    // Stable reference to onSearchSubmit to avoid useEffect dependency issues
    const stableOnSearchSubmit = useCallback(onSearchSubmit, [onSearchSubmit]);

    // Debounced search effect
    useEffect(() => {
        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current);
        }

        if (search.trim()) {
            debounceTimeoutRef.current = setTimeout(() => {
                stableOnSearchSubmit();
            }, 500);
        }

        return () => {
            if (debounceTimeoutRef.current) {
                clearTimeout(debounceTimeoutRef.current);
            }
        };
    }, [search, stableOnSearchSubmit]);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            if (debounceTimeoutRef.current) {
                clearTimeout(debounceTimeoutRef.current);
            }
            stableOnSearchSubmit();
        }
    };

    const handleToggleAdditionalFields = () => {
        setShowAdditionalFields(!showAdditionalFields);
    };

    return (
        <div className={`${styles.container} ${className}`}>
            {/* Search bar */}
            <div className={styles.searchBar}>
                <div className={styles.mainSearch}>
                    <input
                        type="text"
                        value={search}
                        onChange={(e) => onSearchChange(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder={searchPlaceholder}
                        className={commonStyles.muInput}
                    />
                    <button
                        onClick={stableOnSearchSubmit}
                        className={styles.searchButton}
                        disabled={isLoading}
                    >
                        Search
                    </button>
                    <button
                        onClick={onRefresh}
                        className={styles.refreshButton}
                        disabled={isLoading}
                    >
                        Refresh
                    </button>
                    
                    {/* Toggle button for additional fields */}
                    {additionalSearch && additionalSearch.collapsible !== false && (
                        <button
                            onClick={handleToggleAdditionalFields}
                            className={styles.toggleButton}
                            disabled={isLoading}
                        >
                            {showAdditionalFields ? 'Hide Filters' : 'Show Filters'}
                        </button>
                    )}

                    {extraActions}
                </div>

                {/* Additional search fields */}
                {additionalSearch && (
                    <AdditionalSearchFields
                        config={additionalSearch}
                        visible={showAdditionalFields || additionalSearch.collapsible === false}
                        disabled={isLoading}
                    />
                )}
            </div>

            {/* Loading indicator */}
            {isLoading && (
                <div className={styles.loading}>Loading...</div>
            )}

            {/* Table */}
            {!isLoading && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        <EntityTableHeader 
                            columns={columns}
                            sort={sort}
                            onSortChange={onSortChange}
                        />

                        {/* Rows */}
                        <div className={styles.tableRows}>
                            {children}
                        </div>

                        {/* Empty state */}
                        {!children && (
                            <div className={styles.emptyState}>{emptyMessage}</div>
                        )}
                    </div>

                    {/* Pagination */}
                    <Pagination
                        currentPage={pagination.page}
                        totalPages={pagination.totalPages}
                        hasNextPage={pagination.hasNextPage}
                        hasPrevPage={pagination.hasPrevPage}
                        onNextPage={onNextPage}
                        onPrevPage={onPrevPage}
                        onGoToPage={onGoToPage}
                        disabled={isLoading}
                    />
                </>
            )}
        </div>
    );
};

// Separate header component for reusability
interface EntityTableHeaderProps {
    columns: EntityTableColumn[];
    sort: string;
    onSortChange: (sort: string) => void;
}

const EntityTableHeader = ({ columns, sort, onSortChange }: EntityTableHeaderProps) => {
    const currentField = sort?.split(',')[0];
    const currentDir = sort?.split(',')[1] ?? 'asc';

    const toggleSort = (field: string) => {
        if (field === currentField) {
            const newDir = currentDir === 'asc' ? 'desc' : 'asc';
            onSortChange(`${field},${newDir}`);
        } else {
            onSortChange(`${field},desc`);
        }
    };

    const renderLabel = (label: string, field?: string) => {
        if (!field) return label;
        const arrow = field === currentField ? (currentDir === 'asc' ? ' ▲' : ' ▼') : '';
        return label + arrow;
    };

    return (
        <div className={styles.header}>
            {columns.map(column => (
                <div
                    key={column.key}
                    className={`${styles.headerCell} ${column.className || ''} ${
                        column.sortable ? styles.sortable : styles.nonSortable
                    }`}
                    onClick={column.sortable ? () => toggleSort(column.key) : undefined}
                >
                    {renderLabel(column.label, column.sortable ? column.key : undefined)}
                </div>
            ))}
        </div>
    );
};
