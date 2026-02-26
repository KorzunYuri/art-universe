import type { VisibilityState } from '@tanstack/react-table';

const STORAGE_PREFIX = 'column-prefs:';

/**
 * Storage adapter interface for column preferences.
 * Implementations can back this with localStorage, an API, etc.
 */
export interface ColumnPreferencesStorage {
    getPreferences(tableKey: string): VisibilityState | null;
    setPreferences(tableKey: string, prefs: VisibilityState): void;
}

/**
 * localStorage-backed implementation.
 */
export class LocalStorageColumnPreferences implements ColumnPreferencesStorage {
    getPreferences(tableKey: string): VisibilityState | null {
        try {
            const raw = localStorage.getItem(STORAGE_PREFIX + tableKey);
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    }

    setPreferences(tableKey: string, prefs: VisibilityState): void {
        try {
            localStorage.setItem(STORAGE_PREFIX + tableKey, JSON.stringify(prefs));
        } catch {
            // Silently ignore storage errors
        }
    }
}

/**
 * Singleton instance used throughout the app.
 * Swap this to switch storage backends.
 */
export const columnPreferencesStorage: ColumnPreferencesStorage = new LocalStorageColumnPreferences();
