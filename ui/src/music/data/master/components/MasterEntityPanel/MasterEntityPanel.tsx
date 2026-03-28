import { MasterEntityPanelItem } from '../MasterEntityPanelItem/MasterEntityPanelItem.tsx';
import styles from './MasterEntityPanel.module.scss';

export interface MasterEntityPanelProps {
    entities: Array<{ id: number; name: string }>;
    onEntityRemoved?: (entityId: number) => void;
    processingEntities?: Set<number>;
    emptyMessage?: string;
    removeTitle?: string;
    readOnly?: boolean;
}

export const MasterEntityPanel = ({
    entities,
    onEntityRemoved,
    processingEntities = new Set(),
    emptyMessage = "No entities",
    removeTitle = "Remove",
    readOnly = false,
}: MasterEntityPanelProps) => {
    if (entities.length === 0) {
        return (
            <div className={styles.emptyPanel}>
                {emptyMessage}
            </div>
        );
    }

    return (
        <div className={styles.entityPanel}>
            <div className={styles.entitiesList}>
                {entities.map(entity => (
                    <MasterEntityPanelItem
                        key={entity.id}
                        id={entity.id}
                        name={entity.name}
                        onRemove={readOnly ? undefined : onEntityRemoved}
                        isProcessing={processingEntities.has(entity.id)}
                        removeTitle={removeTitle}
                    />
                ))}
            </div>
        </div>
    );
};
