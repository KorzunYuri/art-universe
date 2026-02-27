// hooks
import { useEffect, useRef, memo, useCallback } from 'react';
// components
import { StaticAutocompleteInput } from "@/shared/components";
// types
import type { LookupEntity } from '@/shared/types/lookup';
import type { MasterEntityType } from "@/music/shared/types/entities";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources";
import type { BasicLookupContext } from "@/shared/types/lookup-context";
import type { RawEntityLookupContext } from "@/music/data/raw/shared/types/lookup-context";
// hooks
import { useEntityLookup } from "@/shared/hooks/useEntityLookup.ts";
import { ENTITY_LOOKUP_LIMIT } from "@/music/shared/utils/query-keys.ts";
import styles from './EntityLookup.module.scss';

export interface EntityLookupProps {
    dataSource: DataSource | 'master';
    entityType: MasterEntityType;
    searchString: string;
    context: BasicLookupContext | RawEntityLookupContext;
    onSelect: (entity: LookupEntity | null) => void;
    onChange: (value: string) => void;
    selectedEntity?: LookupEntity | null;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    autoSelectExactMatch?: boolean;
    limit?: number;
}


export const EntityLookup = memo(({
    dataSource,
    entityType,
    searchString,
    context,
    onSelect,
    onChange,
    selectedEntity = null,
    placeholder = "Search...",
    disabled = false,
    className = '',
    autoSelectExactMatch = true,
    limit = ENTITY_LOOKUP_LIMIT
}: EntityLookupProps) => {

    // Combine search string with context and limit
    const lookupParams = {
        search: searchString,
        context,
        limit
    };

    const {
        currentOptions,
        isLoading
    } = useEntityLookup(dataSource, entityType, lookupParams);

    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const lastSearchValue = useRef('');

    // Debounced lookup function
    const debouncedLookup = useCallback((query: string) => {
        // Skip if value hasn't changed since last search
        if (query === lastSearchValue.current) {
            return;
        }
        lastSearchValue.current = query;

        // clear previous timeout
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        // set new timeout
        timeoutRef.current = setTimeout(() => {
            onChange(query);
        }, 300);
    }, [onChange]);

    // Handle value change
    const handleValueChange = useCallback((newValue: string) => {
        onChange(newValue);

        if (newValue.length > 0) {
            debouncedLookup(newValue);
        } else {
            lastSearchValue.current = '';
        }
    }, [onChange, debouncedLookup]);

    // Handle entity selection
    const handleEntitySelect = useCallback((entity: LookupEntity | null) => {
        console.log(`🔄 UniversalEntityLookup: handleEntitySelect called with:`, entity?.name || 'null');
        onSelect(entity);
    }, [onSelect]);

    // Cleanup timeout on unmount
    useEffect(() => {
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    return (
        <div className={styles.root}>
            <StaticAutocompleteInput
                searchString={searchString}
                onChange={handleValueChange}
                onSelect={handleEntitySelect}
                options={currentOptions ?? []}
                selectedEntity={selectedEntity}
                placeholder={placeholder}
                disabled={disabled}
                className={className}
                autoSelectExactMatch={autoSelectExactMatch}
            />

            {isLoading && (
                <div className={styles.loadingIndicator}>
                    ...
                </div>
            )}
        </div>
    );
});
