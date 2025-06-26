// hooks
import { useState, useEffect, useRef } from 'react';
// types
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './AutocompleteInput.module.scss';

export interface SearchableEntity {
    id: number;
    name: string;
}

export interface AutocompleteInputProps {
    value: string;
    onChange: (value: string) => void;
    onSelect: (entity: SearchableEntity) => void;
    searchFunction: (query: string, limit: number) => Promise<ApiResponse<SearchableEntity[]>>;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
}

export const AutocompleteInput = ({
    value,
    onChange,
    onSelect,
    searchFunction,
    placeholder = "Search...",
    disabled = false,
    className = ''
}: AutocompleteInputProps) => {
    const [suggestions, setSuggestions] = useState<SearchableEntity[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [loading, setLoading] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const [hasBeenFocused, setHasBeenFocused] = useState(false);
    
    const inputRef = useRef<HTMLInputElement>(null);
    const suggestionsRef = useRef<HTMLDivElement>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    // Add/remove class to parent row when dropdown is active
    useEffect(() => {
        const inputElement = inputRef.current;
        if (!inputElement) return;

        // Find the parent row element
        let parentRow = inputElement.closest('.row') as HTMLElement;
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

    // Debounced search
    useEffect(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        // Don't search until user has focused the input at least once
        if (!hasBeenFocused) {
            return;
        }

        if (value.trim().length < 2) {
            setSuggestions([]);
            setShowSuggestions(false);
            return;
        }

        timeoutRef.current = setTimeout(async () => {
            setLoading(true);
            try {
                const response = await searchFunction(value.trim(), 10);
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
    }, [value, searchFunction, hasBeenFocused]);

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
    const handleSuggestionClick = (entity: SearchableEntity) => {
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
                    setHasBeenFocused(true);
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

            {/* Debug info - only in development */}
            {process.env.NODE_ENV === 'development' && (
                <div style={{ fontSize: '10px', color: 'gray' }}>
                    Debug: suggestions={suggestions.length}, show={showSuggestions.toString()}
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
