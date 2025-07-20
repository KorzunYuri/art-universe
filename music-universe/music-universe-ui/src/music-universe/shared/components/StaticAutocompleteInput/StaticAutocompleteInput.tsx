// hooks
import { useState, useEffect, useRef } from 'react';
// types
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './StaticAutocompleteInput.module.scss';

export interface ExpStaticAutocompleteInputProps {
    value: string;
    onChange: (value: string) => void;
    onSelect: (entity: LookupEntity | null) => void;
    options: LookupEntity[];
    selectedEntity: LookupEntity | null;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
}

export const StaticAutocompleteInput = ({
    value,
    onChange,
    onSelect,
    options,
    selectedEntity,
    placeholder = "Search...",
    disabled = false,
    className = ''
}: ExpStaticAutocompleteInputProps) => {
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    
    const inputRef = useRef<HTMLInputElement>(null);
    const suggestionsRef = useRef<HTMLDivElement>(null);

    // Filter options based on current value
    const filteredOptions = options.filter(option => 
        option.name.toLowerCase().includes(value.toLowerCase())
    );

    // Check if current value exactly matches any option OR if selectedEntity is set
    const hasExactMatch = value && value.length > 0 && options.some(
        entity => entity.name.toLowerCase() === value.toLowerCase()
    );
    
    // Input should be styled as matched if there's an exact match OR selectedEntity is set
    const shouldStyleAsMatched = hasExactMatch || (selectedEntity !== null);

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 StaticAutocompleteInput MOUNTED with props:', {
            value,
            optionsCount: options.length,
            selectedEntity: selectedEntity?.name || null,
            placeholder,
            disabled,
            className
        });
        return () => {
            console.log('🔧 StaticAutocompleteInput UNMOUNTED');
        };
    }, []);

    // Render logging
    console.log('🔧 StaticAutocompleteInput RENDER with props:', {
        value,
        optionsCount: options.length,
        selectedEntity: selectedEntity?.name || null,
        placeholder,
        disabled,
        className,
        showSuggestions,
        selectedIndex,
        hasExactMatch,
        shouldStyleAsMatched
    });

    // Auto-select exact match when options change
    useEffect(() => {
        if (value && value.length > 0 && options.length > 0 && !selectedEntity) {
            const exactMatch = options.find(
                entity => entity.name.toLowerCase() === value.toLowerCase()
            );
            
            if (exactMatch) {
                console.log(`🔄 ExpStaticAutocompleteInput: Auto-selecting exact match from options: ${exactMatch.name}`);
                onSelect(exactMatch);
            }
        }
    }, [options, value, selectedEntity, onSelect]);

    // Auto-show suggestions when options become available and input is focused
    useEffect(() => {
        // Only show suggestions if:
        // 1. There are filtered options
        // 2. Input is focused (document.activeElement check)
        // 3. No exact match or selectedEntity (not already matched)
        // 4. Input has some value
        if (filteredOptions.length > 0 && 
            inputRef.current === document.activeElement && 
            !shouldStyleAsMatched &&
            value && value.length > 0) {
            console.log(`🔄 ExpStaticAutocompleteInput: Auto-showing suggestions, filteredOptions count: ${filteredOptions.length}`);
            setShowSuggestions(true);
        }
    }, [filteredOptions.length, shouldStyleAsMatched, value]);

    // Add/remove class to parent row when dropdown is active
    useEffect(() => {
        const inputElement = inputRef.current;
        if (!inputElement) return;

        const parentRow = inputElement.closest('.row') as HTMLElement;
        if (!parentRow) return;

        if (showSuggestions && filteredOptions.length > 0) {
            parentRow.classList.add('hasActiveDropdown');
        } else {
            parentRow.classList.remove('hasActiveDropdown');
        }

        return () => {
            if (parentRow) {
                parentRow.classList.remove('hasActiveDropdown');
            }
        };
    }, [showSuggestions, filteredOptions.length]);

    // Handle input change
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        console.log(`🔄 ExpStaticAutocompleteInput: handleInputChange called with: "${newValue}"`);
        console.log(`🔄 ExpStaticAutocompleteInput: Current selectedEntity:`, selectedEntity?.name || 'null');
        console.log(`🔄 ExpStaticAutocompleteInput: Options count:`, options.length);
        
        onChange(newValue);
        
        // Clear selected entity when user types manually (if value doesn't match)
        if (selectedEntity && newValue !== selectedEntity.name) {
            console.log(`🔄 ExpStaticAutocompleteInput: Clearing selectedEntity because value changed`);
            onSelect(null);
        }
        
        // Check for exact match and auto-select if found
        if (newValue && newValue.length > 0) {
            const exactMatch = options.find(
                entity => entity.name.toLowerCase() === newValue.toLowerCase()
            );
            
            console.log(`🔄 ExpStaticAutocompleteInput: Looking for exact match for "${newValue}", found:`, exactMatch?.name || 'none');
            
            if (exactMatch && (!selectedEntity || selectedEntity.id !== exactMatch.id)) {
                console.log(`🔄 ExpStaticAutocompleteInput: Auto-selecting exact match: ${exactMatch.name}`);
                onSelect(exactMatch);
            } else if (!exactMatch && selectedEntity) {
                // If no exact match and we had selectedEntity, clear it
                console.log(`🔄 ExpStaticAutocompleteInput: Clearing selectedEntity, no exact match for: ${newValue}`);
                onSelect(null);
            }
        } else if (selectedEntity) {
            // If input is empty, clear selectedEntity
            console.log(`🔄 ExpStaticAutocompleteInput: Clearing selectedEntity because input is empty`);
            onSelect(null);
        }
        
        // Reset selected index when filtering changes
        setSelectedIndex(-1);
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
        console.log(`🔄 ExpStaticAutocompleteInput: handleSuggestionClick called with:`, entity.name);
        onChange(entity.name);
        onSelect(entity);
        setShowSuggestions(false);
        setSelectedIndex(-1);
    };

    // Handle keyboard navigation
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!showSuggestions || filteredOptions.length === 0) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev => 
                    prev < filteredOptions.length - 1 ? prev + 1 : prev
                );
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < filteredOptions.length) {
                    handleSuggestionClick(filteredOptions[selectedIndex]);
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

    // Handle focus - show suggestions if there are filtered options and no exact match
    const handleFocus = () => {
        if (filteredOptions.length > 0 && !shouldStyleAsMatched) {
            setShowSuggestions(true);
        }
    };

    return (
        <div className={`${styles.container} ${className}`}>
            <input
                ref={inputRef}
                type="text"
                value={value}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                onFocus={handleFocus}
                placeholder={placeholder}
                disabled={disabled}
                className={`${commonStyles.muLabel} ${styles.input} ${shouldStyleAsMatched ? styles.exactMatch : ''}`}
            />

            {filteredOptions.length > 0 && (
                <div 
                    ref={suggestionsRef} 
                    className={styles.suggestions} 
                    style={{
                        display: showSuggestions ? 'block' : 'none',
                        ...getDropdownStyle()
                    }}
                >
                    {filteredOptions.map((entity, index) => (
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
