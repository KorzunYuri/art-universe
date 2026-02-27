import { type ReactNode, useState, useEffect, useRef, useCallback } from 'react';
import {
    useReactTable,
    getCoreRowModel,
    flexRender,
    type ColumnDef,
    type SortingState,
    type VisibilityState,
    type OnChangeFn,
} from '@tanstack/react-table';
import { Pagination } from '../Pagination';
import { AdditionalSearchFields } from '../EntityTable/AdditionalSearchFields';
import type { AdditionalSearchConfig } from '../EntityTable/types';
import type { EntityTablePagination } from '../EntityTable/EntityTable';
import commonStyles from '@/shared/styles/common.module.scss';
import styles from './DataTable.module.scss';

export interface DataTableProps<TData> {
    // Column definitions (TanStack Table)
    columns: ColumnDef<TData, any>[];
    data: TData[];

    // Search
    search: string;
    onSearchChange: (search: string) => void;
    onSearchSubmit: () => void;
    searchPlaceholder?: string;
    additionalSearch?: AdditionalSearchConfig;

    // Sorting (server-side, managed by TanStack Table state)
    sorting: SortingState;
    onSortingChange: OnChangeFn<SortingState>;

    // Pagination (server-side, externally managed)
    pagination: EntityTablePagination;
    onNextPage: () => void;
    onPrevPage: () => void;
    onGoToPage: (page: number) => void;

    // State
    isLoading: boolean;
    onRefresh: () => void;

    // Row interaction
    onRowClick?: (row: TData) => void;
    activeRowId?: number | string | null;
    getRowId?: (row: TData) => string;

    // Expandable rows (e.g., tag panels)
    renderExpandedRow?: (row: TData) => ReactNode;

    // Column visibility
    columnVisibility?: VisibilityState;
    onColumnVisibilityChange?: OnChangeFn<VisibilityState>;

    // Customization
    extraActions?: ReactNode;
    emptyMessage?: string;
    className?: string;
}

export function DataTable<TData>({
    columns,
    data,
    search,
    onSearchChange,
    onSearchSubmit,
    searchPlaceholder = 'Search...',
    additionalSearch,
    sorting,
    onSortingChange,
    pagination,
    onNextPage,
    onPrevPage,
    onGoToPage,
    isLoading,
    onRefresh,
    onRowClick,
    activeRowId,
    getRowId,
    columnVisibility,
    onColumnVisibilityChange,
    renderExpandedRow,
    extraActions,
    emptyMessage = 'No items found',
    className = '',
}: DataTableProps<TData>) {
    const [showAdditionalFields, setShowAdditionalFields] = useState(
        additionalSearch?.defaultCollapsed === false
    );
    const [expandedRowIds, setExpandedRowIds] = useState<Set<string>>(new Set());

    const debounceTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const lastSearchRef = useRef<string>('');

    const stableOnSearchSubmit = useCallback(onSearchSubmit, [onSearchSubmit]);

    // Debounced search
    useEffect(() => {
        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current);
        }

        if (search.trim() && search !== lastSearchRef.current) {
            debounceTimeoutRef.current = setTimeout(() => {
                lastSearchRef.current = search;
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

    const table = useReactTable({
        data,
        columns,
        state: {
            sorting,
            ...(columnVisibility !== undefined ? { columnVisibility } : {}),
        },
        onSortingChange,
        onColumnVisibilityChange,
        getCoreRowModel: getCoreRowModel(),
        manualSorting: true,
        manualPagination: true,
        enableSortingRemoval: false,
        ...(getRowId ? { getRowId } : {}),
    });

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

                    {additionalSearch && additionalSearch.collapsible !== false && (
                        <button
                            onClick={() => setShowAdditionalFields(!showAdditionalFields)}
                            className={styles.toggleButton}
                            disabled={isLoading}
                        >
                            {showAdditionalFields ? 'Hide Filters' : 'Show Filters'}
                        </button>
                    )}

                    {extraActions}
                </div>

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
                        <div className={styles.header}>
                            {table.getHeaderGroups().map(headerGroup => (
                                headerGroup.headers.map(header => {
                                    const canSort = header.column.getCanSort();
                                    const sorted = header.column.getIsSorted();
                                    const meta = header.column.columnDef.meta as Record<string, any> | undefined;
                                    const headerClassName = meta?.headerClassName ?? '';

                                    return (
                                        <div
                                            key={header.id}
                                            className={`${styles.headerCell} ${headerClassName} ${
                                                canSort ? styles.sortable : styles.nonSortable
                                            }`}
                                            onClick={canSort ? header.column.getToggleSortingHandler() : undefined}
                                        >
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(header.column.columnDef.header, header.getContext())}
                                            {sorted === 'asc' && ' \u25B2'}
                                            {sorted === 'desc' && ' \u25BC'}
                                        </div>
                                    );
                                })
                            ))}
                        </div>

                        {/* Rows */}
                        <div className={styles.tableRows}>
                            {table.getRowModel().rows.length > 0 ? (
                                table.getRowModel().rows.map(row => {
                                    const rowId = row.id;
                                    const isActive = activeRowId != null && String(activeRowId) === rowId;
                                    const isExpanded = renderExpandedRow ? expandedRowIds.has(rowId) : false;

                                    const handleClick = () => {
                                        if (onRowClick) {
                                            onRowClick(row.original);
                                        } else if (renderExpandedRow) {
                                            setExpandedRowIds(prev => {
                                                const next = new Set(prev);
                                                if (next.has(rowId)) {
                                                    next.delete(rowId);
                                                } else {
                                                    next.add(rowId);
                                                }
                                                return next;
                                            });
                                        }
                                    };

                                    const isClickable = !!onRowClick || !!renderExpandedRow;

                                    return (
                                        <div key={row.id}>
                                            <div
                                                className={`${styles.row} ${isActive ? styles.activeRow : ''} ${
                                                    isExpanded ? styles.expandedRow : ''
                                                } ${isClickable ? styles.clickable : ''}`}
                                                onClick={isClickable ? handleClick : undefined}
                                            >
                                                {row.getVisibleCells().map(cell => {
                                                    const meta = cell.column.columnDef.meta as Record<string, any> | undefined;
                                                    const cellClassName = meta?.cellClassName ?? '';

                                                    return (
                                                        <div
                                                            key={cell.id}
                                                            className={`${styles.cell} ${cellClassName}`}
                                                        >
                                                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                                        </div>
                                                    );
                                                })}
                                            </div>
                                            {isExpanded && (
                                                <div className={styles.expandedContent}>
                                                    {renderExpandedRow!(row.original)}
                                                </div>
                                            )}
                                        </div>
                                    );
                                })
                            ) : (
                                <div className={styles.emptyState}>{emptyMessage}</div>
                            )}
                        </div>
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
}
