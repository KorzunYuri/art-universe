// hooks
import { useState } from "react";
// components
import { AutocompleteInput, type LookupEntity } from "@/music-universe/shared/components/AutocompleteInput";
// types
import type { Bindable, BoundEntity } from "@/music-universe/shared/types/bindable";
import type { ApiResponse } from "@/music-universe/shared/types/api-response";
// styles
import commonStyles from "@/music-universe/shared/styles/common.module.scss";
import styles from "./EntityBinding.module.scss";

interface EntityBindingProps<T extends Bindable> {
    entity: T;
    onBind: (entityId: number, name: string) => Promise<BoundEntity | null>;
    onUnbind: (entityId: number) => Promise<boolean>;
    onChange: (entity: T) => void;
    lookupFunction?: (query: string, limit?: number) => Promise<ApiResponse<LookupEntity[]>>;
    disabled?: boolean;
}

export function EntityBinding<T extends Bindable>({ 
    entity, 
    onBind, 
    onUnbind, 
    onChange,
    lookupFunction,
    disabled = false 
}: EntityBindingProps<T>) {
    const [entityName, setEntityName] = useState(entity.boundEntity?.referenceName || entity.name);
    const [isBinding, setIsBinding] = useState(false);
    const [isUnbinding, setIsUnbinding] = useState(false);
    const [isEditing, setIsEditing] = useState(!entity.boundEntity);
    async function handleBind() {
        if (!entityName.trim()) return;
        
        setIsBinding(true);
        try {
            const result = await onBind(entity.id, entityName);
            if (result) {
                onChange({
                    ...entity,
                    boundEntity: result
                });
                setIsEditing(false);
            }
        } catch (error) {
            console.error("Failed to bind entity:", error);
        } finally {
            setIsBinding(false);
        }
    }

    async function handleUnbind() {
        setIsUnbinding(true);
        try {
            const success = await onUnbind(entity.id);
            if (success) {
                // Keep the name in the input field for easy re-binding
                setEntityName(entity.boundEntity?.referenceName || entity.name);
                onChange({
                    ...entity,
                    boundEntity: undefined
                });
                setIsEditing(true);
            }
        } catch (error) {
            console.error("Failed to unbind entity:", error);
        } finally {
            setIsUnbinding(false);
        }
    }

    const handleEntitySelect = (selectedEntity: LookupEntity) => {
        console.log(`🔍 Selected existing entity: ${selectedEntity.name} (ID: ${selectedEntity.id})`);
        // Note: For now we still use the name for binding, but we have the ID available for future use
    };

    return (
        <div className={styles.wrapper}>
            {entity.boundEntity && !isEditing ? (
                <div className={styles.wrapper}>
                    <span className={`${commonStyles.muLabel} ${styles.bindingName} ${styles.approvalYes}`}>
                        {entity.boundEntity.referenceName}
                    </span>
                    <button
                        onClick={handleUnbind}
                        disabled={isUnbinding || disabled}
                        className={`${styles.bindingButton}`}
                    >
                        {isUnbinding ? "..." : "Unbind"}
                    </button>
                </div>
            ) : (
                <div className={styles.wrapper}>
                    {lookupFunction ? (
                        <AutocompleteInput
                            value={entityName}
                            onChange={setEntityName}
                            onSelect={handleEntitySelect}
                            lookupFunction={lookupFunction}
                            placeholder="Entity name"
                            disabled={disabled}
                            className={styles.bindingName}
                        />
                    ) : (
                        <input
                            type="text"
                            value={entityName}
                            onChange={(e) => setEntityName(e.target.value)}
                            className={`${commonStyles.muLabel} ${styles.bindingName}`}
                            placeholder="Entity name"
                            disabled={disabled}
                        />
                    )}
                    <button
                        onClick={handleBind}
                        disabled={isBinding || disabled || !entityName.trim()}
                        className={`${styles.bindingButton}`}
                    >
                        {isBinding ? "..." : "Bind"}
                    </button>
                </div>
            )}
        </div>
    );
}
