// hooks
import { useState, useEffect, useRef } from 'react';
// types
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './AutocompleteInput.module.scss';

export interface AutocompleteInputProps {
    value: string;
    onChange: (value: string) => void;
    onSelect: (entity: LookupEntity) => void;
    lookupFunction: (query: string, limit: number) => Promise<ApiResponse<LookupEntity[]>>;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    preloadedOptions?: LookupEntity[]; // Preloaded options for dropdown
}

export const AutocompleteInput = ({
    value,
    onChange,
    onSelect,
    lookupFunction,
    placeholder = "Search...",
    disabled = false,
    className = '',
    preloadedOptions = []
}: AutocompleteInputProps) => {
    const [suggestions, setSuggestions] = useState<LookupEntity[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [loading, setLoading] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const [initialSearchDone, setInitialSearchDone] = useState(false);
    const [lastSearchValue, setLastSearchValue] = useState('');
    
    const inputRef = useRef<HTMLInputElement>(null);
    const suggestionsRef = useRef<HTMLDivElement>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const skipNextLookupRef = useRef(false);
    const hasPreloadedOptionsRef = useRef(false);

    // Set flag if preloadedOptions is provided (even if empty)
    useEffect(() => {
        if (preloadedOptions !== undefined) {
            hasPreloadedOptionsRef.current = true;
        }
    }, [preloadedOptions]);

    // Add/remove class to parent row when dropdown is active
    useEffect(() => {
        const inputElement = inputRef.current;
        if (!inputElement) return;

        // Find the parent row element
        const parentRow = inputElement.closest('.row') as HTMLElement;
        if (!parentRow) return;

        if (showSuggestions && suggestions.length > 0) {
            parentRow.classList.add('hasActiveDropdown');
        } else {
            parentRow.classList.remove('hasActiveDropdown');
        }

        // Cleanup on unmount
        return () => {
            if (parentRow) {
                parentRow.classList.remove('hasActiveDropdown');
            }
        };
    }, [showSuggestions, suggestions.length]);

    // Initial search on component mount if value is provided
    useEffect(() => {
        if (initialSearchDone || !value || value.trim().length < 2) return;

        const performInitialSearch = async () => {
            console.log('🔍 Performing initial search for:', value.trim());
            setLoading(true);
            try {
                // If we have preloadedOptions (even if empty array), use them
                if (hasPreloadedOptionsRef.current) {
                    const filteredOptions = preloadedOptions.filter(option => 
                        option.name.toLowerCase().includes(value.trim().toLowerCase())
                    );
                    
                    if (filteredOptions.length > 0) {
                        console.log('✅ Found matches in preloaded options');
                        setSuggestions(filteredOptions);
                        
                        // Check for exact match
                        const exactMatch = filteredOptions.find(
                            entity => entity.name.toLowerCase() === value.trim().toLowerCase()
                        );
                        
                        if (exactMatch) {
                            console.log('✅ Found exact match in preloaded options:', exactMatch.name);
                            // Set flag to skip next search since this is a programmatic change
                            skipNextLookupRef.current = true;
                            // Select the exact match
                            onSelect(exactMatch);
                        }
                        
                        // Mark as done since we found matches in preloaded options
                        setInitialSearchDone(true);
                        setLoading(false);
                        return;
                    } else {
                        // For initial search, we'll only use API if this is a direct user input
                        // which is unlikely during component mount
                        console.log('⚠️ No matches in preloaded options for initial search');
                        setInitialSearchDone(true);
                        setLoading(false);
                        return;
                    }
                }
                
                // Fall back to API lookup only if preloadedOptions is undefined
                const response = await lookupFunction(value.trim(), 10);
                if (response.success && response.data) {
                    setSuggestions(response.data);
                    
                    // Check for exact match
                    const exactMatch = response.data.find(
                        entity => entity.name.toLowerCase() === value.trim().toLowerCase()
                    );
                    
                    if (exactMatch) {
                        console.log('✅ Found exact match on initial search:', exactMatch.name);
                        // Set flag to skip next search since this is a programmatic change
                        skipNextLookupRef.current = true;
                        // Select the exact match
                        onSelect(exactMatch);
                    }
                }
            } catch (error) {
                console.error('❌ Initial search error:', error);
            } finally {
                setLoading(false);
                setInitialSearchDone(true);
            }
        };

        performInitialSearch();
    }, [value, lookupFunction, onSelect, initialSearchDone, preloadedOptions]);

    // Debounced search
    useEffect(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        // Skip search if this is a programmatic change (after selection)
        if (skipNextLookupRef.current) {
            console.log('⏭️ Skipping search due to programmatic change');
            skipNextLookupRef.current = false;
            return;
        }

        if (value.trim().length < 2) {
            setSuggestions([]);
            setShowSuggestions(false);
            return;
        }

        // Skip if value hasn't changed since last search
        if (value.trim() === lastSearchValue) {
            return;
        }

        timeoutRef.current = setTimeout(async () => {
            setLastSearchValue(value.trim());
            
            // If we have preloadedOptions (even if empty array), use them
            if (hasPreloadedOptionsRef.current) {
                const filteredOptions = preloadedOptions.filter(option => 
                    option.name.toLowerCase().includes(value.trim().toLowerCase())
                );
                
                if (filteredOptions.length > 0) {
                    console.log('✅ Found matches in preloaded options');
                    setSuggestions(filteredOptions);
                    setShowSuggestions(filteredOptions.length > 0);
                    setSelectedIndex(-1);
                    return;
                } else {
                    // Only make API call if user has actually typed something new
                    // This prevents unnecessary API calls after batch lookup
                    const isUserInput = document.activeElement === inputRef.current;
                    if (!isUserInput) {
                        console.log('⏭️ Skipping API call - not from user input');
                        setSuggestions([]);
                        setShowSuggestions(false);
                        return;
                    }
                    
                    console.log('⚠️ No matches in preloaded options, falling back to API call');
                    // Continue to API lookup below
                }
            }
            
            // Fall back to API lookup only if preloadedOptions is undefined
            setLoading(true);
            try {
                const response = await lookupFunction(value.trim(), 10);
                if (response.success && response.data) {
                    setSuggestions(response.data);
                    setShowSuggestions(response.data.length > 0);
                    setSelectedIndex(-1);
                } else {
                    setSuggestions([]);
                    setShowSuggestions(false);
                }
            } catch (error) {
                console.error('❌ Search error:', error);
                setSuggestions([]);
                setShowSuggestions(false);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [value, lookupFunction, initialSearchDone, preloadedOptions, lastSearchValue]);

    // Handle input change
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    // Calculate dropdown position
    const getDropdownStyle = (): React.CSSProperties => {
        if (!inputRef.current) return {};
        
        const rect = inputRef.current.getBoundingClientRect();
        return {
            position: 'fixed',
            top: rect.bottom,
            left: rect.left,
            width: rect.width,
            zIndex: 10000
        };
    };

    // Handle suggestion click
    const handleSuggestionClick = (entity: LookupEntity) => {
        // Set flag to skip next search since this is a programmatic change
        skipNextLookupRef.current = true;
        // Update the input value with selected entity name
        onChange(entity.name);
        onSelect(entity);
        setShowSuggestions(false);
        setSelectedIndex(-1);
    };

    // Handle keyboard navigation
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!showSuggestions || suggestions.length === 0) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev => 
                    prev < suggestions.length - 1 ? prev + 1 : prev
                );
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
                    handleSuggestionClick(suggestions[selectedIndex]);
                }
                break;
            case 'Escape':
                setShowSuggestions(false);
                setSelectedIndex(-1);
                break;
        }
    };

    // Handle click outside to close suggestions
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                inputRef.current && 
                !inputRef.current.contains(event.target as Node) &&
                suggestionsRef.current &&
                !suggestionsRef.current.contains(event.target as Node)
            ) {
                setShowSuggestions(false);
                setSelectedIndex(-1);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    return (
        <div className={`${styles.container} ${className}`}>
            <input
                ref={inputRef}
                type="text"
                value={value}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                onFocus={() => {
                    if (suggestions.length > 0) {
                        setShowSuggestions(true);
                    }
                }}
                placeholder={placeholder}
                disabled={disabled}
                className={`${commonStyles.muLabel} ${styles.input}`}
            />
            
            {loading && (
                <div className={styles.loadingIndicator}>
                    <span>...</span>
                </div>
            )}

            {suggestions.length > 0 && (
                <div 
                    ref={suggestionsRef} 
                    className={styles.suggestions} 
                    style={{
                        display: showSuggestions ? 'block' : 'none',
                        ...getDropdownStyle()
                    }}
                >
                    {suggestions.map((entity, index) => (
                        <div
                            key={entity.id}
                            className={`${styles.suggestion} ${
                                index === selectedIndex ? styles.selected : ''
                            }`}
                            onClick={() => handleSuggestionClick(entity)}
                        >
                            {entity.name}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
