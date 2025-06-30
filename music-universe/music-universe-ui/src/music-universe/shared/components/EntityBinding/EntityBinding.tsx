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
    onBindToExisting: (entityId: number, targetEntityId: number) => Promise<BoundEntity | null>;
    onCreateAndBind: (entityId: number, name: string) => Promise<BoundEntity | null>;
    onUnbind: (entityId: number) => Promise<boolean>;
    onBeforeBind: (entity: T) => Promise<boolean>;
    onAfterBind: (entity: T) => void;
    lookupFunction?: (query: string, limit?: number) => Promise<ApiResponse<LookupEntity[]>>;
    disabled?: boolean;
}

export function EntityBinding<T extends Bindable>({ 
    entity, 
    onBindToExisting,
    onCreateAndBind,
    onUnbind, 
    onBeforeBind,
    onAfterBind,
    lookupFunction,
    disabled = false 
}: EntityBindingProps<T>) {
    const [entityName, setEntityName] = useState(entity.boundEntity?.referenceName || entity.name);
    const [isBinding, setIsBinding] = useState(false);
    const [isUnbinding, setIsUnbinding] = useState(false);
    const [isEditing, setIsEditing] = useState(!entity.boundEntity);
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);

    async function handleBind() {
        if (!entityName.trim()) return;
        
        setIsBinding(true);
        try {
            // Execute pre-bind action
            console.log('🔄 Executing pre-bind action...');
            const canProceed = await onBeforeBind(entity);
            
            if (!canProceed) {
                console.log('❌ Pre-bind action failed, aborting bind operation');
                return;
            }
            
            console.log('✅ Pre-bind action successful, proceeding with bind');
            
            let result: BoundEntity | null = null;
            
            // If an entity was selected from autocomplete, bind to existing
            if (selectedEntity) {
                console.log(`🔗 Binding to existing entity: ${selectedEntity.name} (ID: ${selectedEntity.id})`);
                result = await onBindToExisting(entity.id, selectedEntity.id);
            } 
            // Otherwise, create new entity and bind
            else {
                console.log(`🔗 Creating new entity and binding: ${entityName}`);
                result = await onCreateAndBind(entity.id, entityName);
            }
            
            if (result) {
                onAfterBind({
                    ...entity,
                    boundEntity: result
                });
                setIsEditing(false);
                setSelectedEntity(null);
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
                onAfterBind({
                    ...entity,
                    boundEntity: undefined
                });
                setIsEditing(true);
                setSelectedEntity(null);
            }
        } catch (error) {
            console.error("Failed to unbind entity:", error);
        } finally {
            setIsUnbinding(false);
        }
    }

    const handleEntitySelect = (selectedEntity: LookupEntity) => {
        console.log(`🔍 Selected existing entity: ${selectedEntity.name} (ID: ${selectedEntity.id})`);
        setSelectedEntity(selectedEntity);
        setEntityName(selectedEntity.name);
    };

    const handleInputChange = (value: string) => {
        setEntityName(value);
        // Clear selected entity when user types manually
        if (selectedEntity && value !== selectedEntity.name) {
            setSelectedEntity(null);
        }
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
                            onChange={handleInputChange}
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
                            onChange={(e) => handleInputChange(e.target.value)}
                            className={`${commonStyles.muLabel} ${styles.bindingName}`}
                            placeholder="Entity name"
                            disabled={disabled}
                        />
                    )}
                    <button
                        onClick={handleBind}
                        disabled={isBinding || disabled || !entityName.trim()}
                        className={`${styles.bindingButton}`}
                        title={selectedEntity ? `Bind to existing: ${selectedEntity.name}` : `Create new: ${entityName}`}
                    >
                        {isBinding ? "..." : (selectedEntity ? "Link" : "Create")}
                    </button>
                </div>
            )}
        </div>
    );
}
