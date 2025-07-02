// hooks
import { useState, useEffect } from 'react';
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './EditableText.module.scss';

interface EditableTextProps {
    value: string;
    onChange: (value: string) => void;
    onDirtyChange?: (isDirty: boolean) => void;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
}

export const EditableText = ({
    value,
    onChange,
    onDirtyChange,
    placeholder = "",
    disabled = false,
    className = ''
}: EditableTextProps) => {
    const [internalValue, setInternalValue] = useState(value);
    const [originalValue, setOriginalValue] = useState(value);
    const [isDirty, setIsDirty] = useState(false);

    console.log('🔧 EditableText render:', { value, internalValue, originalValue, isDirty, placeholder });

    // Update internal value when external value changes
    useEffect(() => {
        console.log('🔄 EditableText useEffect (value change):', { oldValue: internalValue, newValue: value });
        setInternalValue(value);
        setOriginalValue(value);
        setIsDirty(false);
    }, [value]);

    // Notify parent about dirty state changes
    useEffect(() => {
        console.log('🔔 EditableText useEffect (dirty change):', { isDirty });
        if (onDirtyChange) {
            onDirtyChange(isDirty);
        }
    }, [isDirty, onDirtyChange]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        const isValueDirty = newValue !== originalValue;
        console.log('✏️ EditableText handleChange:', { 
            newValue, 
            originalValue, 
            isValueDirty,
            comparison: `"${newValue}" !== "${originalValue}"`
        });
        
        setInternalValue(newValue);
        setIsDirty(isValueDirty);
        onChange(newValue);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Escape') {
            console.log('⎋ EditableText Escape pressed - resetting to original value:', originalValue);
            // Reset to original value
            setInternalValue(originalValue);
            setIsDirty(false);
            onChange(originalValue);
        }
    };

    return (
        <input
            type="text"
            value={internalValue}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            disabled={disabled}
            className={`${commonStyles.muLabel} ${styles.editableText} ${isDirty ? styles.dirty : ''} ${className}`}
        />
    );
};
