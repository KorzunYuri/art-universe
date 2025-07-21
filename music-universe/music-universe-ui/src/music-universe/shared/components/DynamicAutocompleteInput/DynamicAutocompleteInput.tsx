// hooks
import { useState, useEffect, useRef } from 'react';
// components
import {StaticAutocompleteInput} from "@/music-universe/shared/components";
// types
import type { LookupEntity } from '@/music-universe/shared/types/lookup';

export interface ExpDynamicAutocompleteInputProps {
    value: string;
    onChange: (value: string) => void;
    onSelect: (entity: LookupEntity | null) => void;
    lookupFunction: (query: string, limit?: number) => Promise<LookupEntity[]>;
    preloadedOptions?: LookupEntity[];
    selectedEntity: LookupEntity | null;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
}

export const DynamicAutocompleteInput = ({
    value,
    onChange,
    onSelect,
    lookupFunction,
    preloadedOptions = [],
    selectedEntity,
    placeholder = "Search...",
    disabled = false,
    className = ''
}: ExpDynamicAutocompleteInputProps) => {
    const [lookupResults, setLookupResults] = useState<LookupEntity[]>([]);
    const [hasUserInput, setHasUserInput] = useState(false);
    const [loading, setLoading] = useState(false);
    
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const lastSearchValue = useRef('');

    // Show either preloaded options (if no user input) or lookup results
    const currentOptions = hasUserInput ? lookupResults : preloadedOptions;

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 DynamicAutocompleteInput MOUNTED with props:', {
            value,
            preloadedOptionsCount: preloadedOptions.length,
            selectedEntity: selectedEntity?.name || null,
            placeholder,
            disabled,
            className
        });
        return () => {
            console.log('🔧 DynamicAutocompleteInput UNMOUNTED');
        };
    }, []);

    // Render logging
    console.log('🔧 DynamicAutocompleteInput RENDER with props:', {
        value,
        preloadedOptionsCount: preloadedOptions.length,
        selectedEntity: selectedEntity?.name || null,
        placeholder,
        disabled,
        className,
        hasUserInput,
        lookupResultsCount: lookupResults.length,
        currentOptionsCount: currentOptions.length,
        loading
    });

    // Debounced lookup function
    const debouncedLookup = async (query: string) => {
        // Skip if value hasn't changed since last search
        if (query === lastSearchValue.current) {
            return;
        }

        lastSearchValue.current = query;
        setLoading(true);
        
        try {
            const response = await lookupFunction(query, 10);
            if (response.success && response.data) {
                console.log(`🔄 ExpDynamicAutocompleteInput: Lookup successful, found ${response.data.length} results for "${query}"`);
                setLookupResults(response.data);
                
                // Check for exact match in lookup results and auto-select if not already selected
                if (!selectedEntity) {
                    const exactMatch = response.data.find(
                        entity => entity.name.toLowerCase() === query.toLowerCase()
                    );
                    
                    if (exactMatch) {
                        console.log(`🔄 ExpDynamicAutocompleteInput: Auto-selecting exact match from lookup: ${exactMatch.name}`);
                        onSelect(exactMatch);
                    }
                }
            } else {
                console.log(`❌ ExpDynamicAutocompleteInput: Lookup failed or returned no data for "${query}"`);
                setLookupResults([]);
            }
        } catch (error) {
            console.error('❌ Lookup error:', error);
            setLookupResults([]);
        } finally {
            setLoading(false);
        }
    };

    // Handle value change
    const handleValueChange = (newValue: string) => {
        onChange(newValue);
        
        // Clear timeout if exists
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        if (newValue.length > 0) {
            setHasUserInput(true);
            
            // Debounce lookup call
            timeoutRef.current = setTimeout(() => {
                debouncedLookup(newValue);
            }, 300);
        } else {
            // If input is cleared - return to preloaded options
            setHasUserInput(false);
            setLookupResults([]);
            lastSearchValue.current = '';
        }
    };

    // Handle entity selection
    const handleEntitySelect = (entity: LookupEntity | null) => {
        console.log(`🔄 ExpDynamicAutocompleteInput: handleEntitySelect called with:`, entity?.name || 'null');
        console.log(`🔄 ExpDynamicAutocompleteInput: Calling onSelect with:`, entity?.name || 'null');
        onSelect(entity);
    };

    // Cleanup timeout on unmount
    useEffect(() => {
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    // Reset state when preloadedOptions change
    useEffect(() => {
        if (!hasUserInput) {
            // If we're showing preloaded options and they changed, update display
            // This handles the case when preloadedOptions are updated from parent
            
            // Check for exact match in preloaded options and auto-select if not already selected
            if (value && value.length > 0 && !selectedEntity) {
                const exactMatch = preloadedOptions.find(
                    entity => entity.name.toLowerCase() === value.toLowerCase()
                );
                
                if (exactMatch) {
                    console.log(`🔄 ExpDynamicAutocompleteInput: Auto-selecting exact match from preloaded: ${exactMatch.name}`);
                    onSelect(exactMatch);
                }
            }
        }
    }, [preloadedOptions, hasUserInput, value, selectedEntity, onSelect]);

    return (
        <div style={{ position: 'relative', display: 'flex', flex: 1 }}>
            <StaticAutocompleteInput
                value={value}
                onChange={handleValueChange}
                onSelect={handleEntitySelect}
                options={currentOptions}
                selectedEntity={selectedEntity}
                placeholder={placeholder}
                disabled={disabled}
                className={className}
            />
            
            {loading && (
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
};
