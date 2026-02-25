import { useState, useCallback, useMemo } from 'react';
import type { VisibilityState, OnChangeFn } from '@tanstack/react-table';
import { columnPreferencesStorage } from '../services/ColumnPreferencesStorage';

export interface ColumnConfig {
    id: string;
    label: string;
    defaultVisible?: boolean;
}

interface UseColumnPreferencesResult {
    columnVisibility: VisibilityState;
    onColumnVisibilityChange: OnChangeFn<VisibilityState>;
    resetToDefaults: () => void;
    /** The configurable columns with their current visibility state */
    configurableColumns: Array<ColumnConfig & { visible: boolean }>;
    toggleColumn: (columnId: string) => void;
}

/**
 * Hook for managing configurable column visibility with localStorage persistence.
 *
 * @param tableKey Unique key for this table (e.g., "master-artists")
 * @param configurableColumnDefs The columns that can be toggled (fixed columns are always visible)
 */
export function useColumnPreferences(
    tableKey: string,
    configurableColumnDefs: ColumnConfig[],
): UseColumnPreferencesResult {
    const defaultVisibility = useMemo(() => {
        const vis: VisibilityState = {};
        for (const col of configurableColumnDefs) {
            vis[col.id] = col.defaultVisible ?? false;
        }
        return vis;
    }, [configurableColumnDefs]);

    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(() => {
        const stored = columnPreferencesStorage.getPreferences(tableKey);
        return stored ?? defaultVisibility;
    });

    const onColumnVisibilityChange: OnChangeFn<VisibilityState> = useCallback(
        (updaterOrValue) => {
            setColumnVisibility(prev => {
                const next = typeof updaterOrValue === 'function'
                    ? updaterOrValue(prev)
                    : updaterOrValue;
                columnPreferencesStorage.setPreferences(tableKey, next);
                return next;
            });
        },
        [tableKey],
    );

    const resetToDefaults = useCallback(() => {
        setColumnVisibility(defaultVisibility);
        columnPreferencesStorage.setPreferences(tableKey, defaultVisibility);
    }, [tableKey, defaultVisibility]);

    const toggleColumn = useCallback(
        (columnId: string) => {
            setColumnVisibility(prev => {
                const next = { ...prev, [columnId]: !prev[columnId] };
                columnPreferencesStorage.setPreferences(tableKey, next);
                return next;
            });
        },
        [tableKey],
    );

    const configurableColumns = useMemo(
        () =>
            configurableColumnDefs.map(col => ({
                ...col,
                visible: columnVisibility[col.id] ?? col.defaultVisible ?? false,
            })),
        [configurableColumnDefs, columnVisibility],
    );

    return {
        columnVisibility,
        onColumnVisibilityChange,
        resetToDefaults,
        configurableColumns,
        toggleColumn,
    };
}
