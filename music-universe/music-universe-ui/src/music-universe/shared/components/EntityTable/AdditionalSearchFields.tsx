import { type AdditionalSearchConfig, type SearchField } from './types';
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './AdditionalSearchFields.module.scss';

interface AdditionalSearchFieldsProps {
    config: AdditionalSearchConfig;
    visible: boolean;
    disabled: boolean;
}

export const AdditionalSearchFields = ({
    config,
    visible,
    disabled
}: AdditionalSearchFieldsProps) => {
    if (!visible) {
        return null;
    }

    const renderField = (field: SearchField) => {
        switch (field.type) {
            case 'text':
                return (
                    <input
                        key={field.key}
                        type="text"
                        value={field.value}
                        onChange={(e) => field.onChange(e.target.value)}
                        placeholder={field.placeholder}
                        disabled={disabled || field.disabled}
                        className={commonStyles.muInput}
                    />
                );

            case 'number':
                return (
                    <input
                        key={field.key}
                        type="number"
                        value={field.value}
                        onChange={(e) => {
                            const value = e.target.value;
                            field.onChange(value === '' ? '' : Number(value));
                        }}
                        placeholder={field.placeholder}
                        min={field.min}
                        max={field.max}
                        step={field.step}
                        disabled={disabled || field.disabled}
                        className={commonStyles.muInput}
                    />
                );

            case 'select':
                return (
                    <select
                        key={field.key}
                        value={field.value}
                        onChange={(e) => {
                            const value = e.target.value;
                            field.onChange(value === '' ? '' : 
                                (isNaN(Number(value)) ? value : Number(value)));
                        }}
                        disabled={disabled || field.disabled}
                        className={commonStyles.muInput}
                    >
                        <option value="">{field.placeholder || `Select ${field.label}`}</option>
                        {field.options.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                );

            case 'multiselect':
                return (
                    <div key={field.key} className={styles.multiselectContainer}>
                        <label className={styles.multiselectLabel}>{field.label}:</label>
                        <div className={styles.multiselectOptions}>
                            {field.options.map(option => (
                                <label key={option.value} className={styles.checkboxLabel}>
                                    <input
                                        type="checkbox"
                                        checked={field.value.includes(option.value)}
                                        onChange={(e) => {
                                            if (e.target.checked) {
                                                field.onChange([...field.value, option.value]);
                                            } else {
                                                field.onChange(field.value.filter(v => v !== option.value));
                                            }
                                        }}
                                        disabled={disabled || field.disabled}
                                    />
                                    {option.label}
                                </label>
                            ))}
                        </div>
                    </div>
                );

            case 'custom':
                return (
                    <div key={field.key} className={styles.customField}>
                        {field.render()}
                    </div>
                );

            default:
                return null;
        }
    };

    return (
        <div className={styles.additionalFields}>
            {config.title && (
                <h4 className={styles.title}>{config.title}</h4>
            )}
            <div className={styles.fieldsGrid}>
                {config.fields.map(field => (
                    <div key={field.key} className={styles.fieldContainer}>
                        {field.type !== 'multiselect' && field.type !== 'custom' && (
                            <label className={styles.fieldLabel}>{field.label}:</label>
                        )}
                        {renderField(field)}
                    </div>
                ))}
            </div>
        </div>
    );
};
