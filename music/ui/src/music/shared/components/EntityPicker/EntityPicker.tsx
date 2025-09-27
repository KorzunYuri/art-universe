import { useState } from 'react';
import { EntityLookup } from '@/music/shared/components';
import type { LookupEntity } from '@/music/shared/types/lookup';
import type { DataSource } from '@/music/data/raw/shared/types/data-sources';
import type { MasterEntityType } from '@/music/shared/types/entities';
import { LookupContextFactory } from '@/music/shared/types/lookup-context';
import styles from './EntityPicker.module.scss';

export interface EntityPickerProps {
    dataSource: DataSource | 'master';
    entityType: MasterEntityType;
    buttonLabel: string;
    onEntitySelected?: (entity: LookupEntity) => void;
    disabled?: boolean;
}

export const EntityPicker = ({
    dataSource,
    entityType,
    buttonLabel,
    onEntitySelected,
    disabled = false
}: EntityPickerProps) => {
    const [searchString, setSearchString] = useState('');
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);

    const handleEntitySelect = (entity: LookupEntity | null) => {
        setSelectedEntity(entity);
    };

    const handleSearchChange = (value: string) => {
        setSearchString(value);
        if (selectedEntity && value !== selectedEntity.name) {
            setSelectedEntity(null);
        }
    };

    const handleAdd = () => {
        if (!selectedEntity || disabled) return;

        onEntitySelected?.(selectedEntity);
        
        setSearchString('');
        setSelectedEntity(null);
    };

    return (
        <div className={styles.picker}>
            <div className={styles.lookupContainer}>
                <EntityLookup
                    dataSource={dataSource}
                    entityType={entityType}
                    searchString={searchString}
                    context={LookupContextFactory.basic()}
                    onChange={handleSearchChange}
                    onSelect={handleEntitySelect}
                    selectedEntity={selectedEntity}
                    placeholder={`Search for ${entityType}`}
                    disabled={disabled}
                />
            </div>
            <button
                className={styles.addButton}
                onClick={handleAdd}
                disabled={!selectedEntity || disabled}
            >
                {buttonLabel}
            </button>
        </div>
    );
};
