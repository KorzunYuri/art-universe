// hooks
import {useEffect, useRef, memo, useCallback} from 'react';
// components
import {StaticAutocompleteInput} from "@/music-universe/shared/components";
// types
import type { LookupEntity } from '@/music-universe/music-data/types/master-entities-lookup.ts';
import {useMasterEntitiesLookup} from "@/music-universe/music-data/hooks/useMasterEntitiesLookup.ts";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";

export interface MasterEntityLookupProps {
    entityType: MasterEntityType,
    searchString: string;
    onSelect: (entity: LookupEntity | null) => void;
    onChange: (value: string) => void;
    selectedEntity?: LookupEntity | null;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    autoSelectExactMatch?: boolean;
}

export const MasterEntityLookup = memo(({
    entityType,
    searchString,
    onSelect,
    onChange,
    selectedEntity = null,
    placeholder = "Search...",
    disabled = false,
    className = '',
    autoSelectExactMatch = true
}: MasterEntityLookupProps) => {

    const {
        currentOptions,
        setQuery,
        isLoading
    } = useMasterEntitiesLookup(entityType, searchString);
    
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
            console.log(`🔍 MasterEntityLookup: Executing search for "${query}"`);
            setQuery(query);
        }, 300);
    }, [setQuery]);

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
        console.log(`🔄 MasterEntityLookup: handleEntitySelect called with:`, entity?.name || 'null');
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
        <div style={{ position: 'relative', display: 'flex', flex: 1 }}>
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
                <div style={{
                    position: 'absolute',
                    right: '8px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    fontSize: '12px',
                    color: '#666'
                }}>
                    ...
                </div>
            )}
        </div>
    );
});
