// hooks
import {useState, useEffect, useRef, memo, useMemo} from 'react';
// types
import type { LookupEntity } from '@/music-universe/shared/types/lookup.ts';
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './StaticAutocompleteInput.module.scss';

export interface StaticAutocompleteInputProps {
    searchString: string;
    onChange: (value: string) => void;
    onSelect: (entity: LookupEntity | null) => void;
    options: LookupEntity[];
    selectedEntity: LookupEntity | null;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    autoSelectExactMatch?: boolean;
}

export const StaticAutocompleteInput = memo(({
    searchString,
    onChange,
    onSelect,
    options,
    selectedEntity,
    placeholder = "Search...",
    disabled = false,
    className = '',
    autoSelectExactMatch = true
}: StaticAutocompleteInputProps) => {
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);

    const inputRef = useRef<HTMLInputElement>(null);
    const suggestionsRef = useRef<HTMLDivElement>(null);
    const autoSelectProcessedRef = useRef(false);
    const initialRenderRef = useRef(true);

    const filteredOptions = useMemo(() =>
            options.filter(option =>
                option.name.toLowerCase().includes(searchString.toLowerCase())
            ),
        [options, searchString]
    );

    const exactMatch = useMemo(() => {
        if (!searchString || searchString.length === 0 || !options.length) return null;
        return options.find(
            entity => entity.name.toLowerCase() === searchString.toLowerCase()
        ) || null;
    }, [options, searchString]);

    const hasExactMatch = exactMatch !== null;

    const shouldStyleAsMatched = hasExactMatch || (selectedEntity !== null);

    // automatically choose match on options change
    useEffect(() => {
        // Пропускаем первый рендер
        if (initialRenderRef.current) {
            initialRenderRef.current = false;
            return;
        }

        // skip if the same entity is already chosen
        if (selectedEntity && exactMatch && selectedEntity.id === exactMatch.id) {
            return;
        }

        // select exact match if it exists
        if (autoSelectExactMatch && exactMatch && !autoSelectProcessedRef.current) {
            console.log(`🔄 StaticAutocompleteInput: Auto-selecting exact match: ${exactMatch.name}`);
            autoSelectProcessedRef.current = true;
            onSelect(exactMatch);
        }
    }, [exactMatch, selectedEntity, onSelect, autoSelectExactMatch]);

    // reset auto select processing flag when search string is changed
    useEffect(() => {
        autoSelectProcessedRef.current = false;
    }, [searchString]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        console.log(`🔄 StaticAutocompleteInput: handleInputChange called with: "${newValue}"`);

        onChange(newValue);

        // reset selection on input change
        setSelectedIndex(-1);

        // hide the list if input is blank
        if (!newValue) {
            setShowSuggestions(false);
        } else {
            setShowSuggestions(true);
        }
    };

    const handleOptionClick = (entity: LookupEntity) => {
        console.log(`🔄 StaticAutocompleteInput: handleSuggestionClick called with: ${entity.name}`);
        onChange(entity.name);
        onSelect(entity);
        setShowSuggestions(false);
        setSelectedIndex(-1);
    };

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
                    handleOptionClick(filteredOptions[selectedIndex]);
                }
                break;
            case 'Escape':
                setShowSuggestions(false);
                setSelectedIndex(-1);
                break;
        }
    };

    const handleFocus = () => {
        if (filteredOptions.length > 0 && !shouldStyleAsMatched && searchString) {
            setShowSuggestions(true);
        }
    };

    // calc dropdown position
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

    // add class to the parent
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

    return (
        <div className={`${styles.container} ${className}`}>
            <input
                ref={inputRef}
                type="text"
                value={searchString}
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
                            onClick={() => handleOptionClick(entity)}
                        >
                            {entity.name}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
});
