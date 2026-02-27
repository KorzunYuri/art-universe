import type {ReactNode} from 'react';

/**
 * Base interface for search field configuration
 */
export interface BaseSearchField {
    key: string;
    label: string;
    placeholder?: string;
    disabled?: boolean;
}

/**
 * Text input search field
 */
export interface TextSearchField extends BaseSearchField {
    type: 'text';
    value: string;
    onChange: (value: string) => void;
}

/**
 * Number input search field
 */
export interface NumberSearchField extends BaseSearchField {
    type: 'number';
    value: number | '';
    onChange: (value: number | '') => void;
    min?: number;
    max?: number;
    step?: number;
}

/**
 * Select dropdown search field
 */
export interface SelectSearchField extends BaseSearchField {
    type: 'select';
    value: string | number | '';
    onChange: (value: string | number | '') => void;
    options: Array<{
        value: string | number;
        label: string;
    }>;
    multiple?: boolean;
}

/**
 * Multi-select search field (for arrays)
 */
export interface MultiSelectSearchField extends BaseSearchField {
    type: 'multiselect';
    value: (string | number)[];
    onChange: (value: (string | number)[]) => void;
    options: Array<{
        value: string | number;
        label: string;
    }>;
}

/**
 * Custom search field with custom render function
 */
export interface CustomSearchField extends BaseSearchField {
    type: 'custom';
    render: () => ReactNode;
}

/**
 * Union type for all search field types
 */
export type SearchField = 
    | TextSearchField 
    | NumberSearchField 
    | SelectSearchField 
    | MultiSelectSearchField 
    | CustomSearchField;

/**
 * Configuration for additional search fields
 */
export interface AdditionalSearchConfig {
    fields: SearchField[];
    collapsible?: boolean;
    defaultCollapsed?: boolean;
    title?: string;
}
