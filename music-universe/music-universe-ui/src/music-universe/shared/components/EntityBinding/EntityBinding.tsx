// hooks
import { useState, useEffect, useCallback } from "react";
// components
import {DynamicAutocompleteInput} from "@/music-universe/shared/components";
// types
import type { RawEntity, MasterEntity } from "@/music-universe/shared/types/entity-reference";
import type { ApiResponse } from "@/music-universe/shared/types/api-response";
import type { LookupEntity } from "@/music-universe/shared/types/lookup";
// styles
import commonStyles from "@/music-universe/shared/styles/common.module.scss";
import styles from "./EntityBinding.module.scss";

interface ExpEntityBindingProps<T extends RawEntity> {
    entity: T;
    onBindToExisting: (entityId: number, targetEntityId: number) => Promise<MasterEntity | null>;
    onCreateAndBind: (entityId: number, name: string) => Promise<MasterEntity | null>;
    onUnbind: (entityId: number) => Promise<boolean>;
    onBeforeBind: (entity: T) => Promise<boolean>;
    onAfterBind: (entity: T) => void;
    lookupFunction: (query: string, limit?: number) => Promise<ApiResponse<LookupEntity[]>>;
    preloadedOptions?: LookupEntity[];
    disabled?: boolean;
}

enum BindingState {
    UNBOUND_NO_MATCH = 'unbound_no_match',    // Create button
    UNBOUND_WITH_MATCH = 'unbound_with_match', // Link button  
    BOUND = 'bound'                            // Unbind button
}

export const EntityBinding = <T extends RawEntity>({
    entity, 
    onBindToExisting,
    onCreateAndBind,
    onUnbind, 
    onBeforeBind,
    onAfterBind,
    lookupFunction,
    preloadedOptions = [],
    disabled = false 
}: ExpEntityBindingProps<T>) => {
    const [inputValue, setInputValue] = useState(entity.masterEntity?.name || entity.name);
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);
    const [isBinding, setIsBinding] = useState(false);
    const [isUnbinding, setIsUnbinding] = useState(false);

    // Determine current binding state
    const getBindingState = (): BindingState => {
        if (entity.masterEntity) {
            return BindingState.BOUND;
        }
        
        if (selectedEntity) {
            return BindingState.UNBOUND_WITH_MATCH;
        }
        
        return BindingState.UNBOUND_NO_MATCH;
    };

    const bindingState = getBindingState();

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 EntityBinding MOUNTED with props:', {
            entityId: entity.id,
            entityName: entity.name,
            hasMasterEntity: !!entity.masterEntity,
            masterEntityName: entity.masterEntity?.name || null,
            preloadedOptionsCount: preloadedOptions.length,
            disabled
        });
        return () => {
            console.log('🔧 EntityBinding UNMOUNTED for entity:', entity.id);
        };
    }, []);

    // Render logging
    console.log('🔧 EntityBinding RENDER with props:', {
        entityId: entity.id,
        entityName: entity.name,
        hasMasterEntity: !!entity.masterEntity,
        masterEntityName: entity.masterEntity?.name || null,
        preloadedOptionsCount: preloadedOptions.length,
        disabled,
        inputValue,
        selectedEntity: selectedEntity?.name || null,
        isBinding,
        isUnbinding,
        bindingState: bindingState
    });

    // Update inputValue when entity.masterEntity changes
    useEffect(() => {
        setInputValue(entity.masterEntity?.name || entity.name);
    }, [entity.masterEntity, entity.name]);

    // Try to find selectedEntity in preloadedOptions when they change
    useEffect(() => {
        if (!entity.masterEntity && inputValue && preloadedOptions.length > 0) {
            const matchingOption = preloadedOptions.find(
                option => option.name.toLowerCase() === inputValue.toLowerCase()
            );
            
            if (matchingOption && !selectedEntity) {
                console.log(`🔄 ExpEntityBinding: Found matching option in preloadedOptions: ${matchingOption.name}`);
                setSelectedEntity(matchingOption);
            } else if (selectedEntity) {
                // Clear selectedEntity if it's no longer in preloadedOptions
                const stillExists = preloadedOptions.some(option => option.id === selectedEntity.id);
                if (!stillExists) {
                    console.log(`❌ ExpEntityBinding: Selected entity no longer in preloadedOptions, clearing`);
                    setSelectedEntity(null);
                }
            }
        }
    }, [preloadedOptions, inputValue, entity.masterEntity]);

    // Atomic entity update helper
    const updateEntityAtomically = (updates: Partial<T>) => {
        Object.assign(entity, updates);
    };

    // Handle binding operations
    const handleBind = useCallback(async () => {
        if (!inputValue) return;
        
        setIsBinding(true);
        try {
            // Execute pre-bind action
            const canProceed = await onBeforeBind(entity);
            
            if (!canProceed) {
                return;
            }
            
            let result: MasterEntity | null = null;
            let wasCreateOperation = false;
            
            // If an entity was selected from autocomplete, bind to existing
            if (selectedEntity) {
                result = await onBindToExisting(entity.id, selectedEntity.id);
            } 
            // Otherwise, create new entity and bind
            else {
                result = await onCreateAndBind(entity.id, inputValue);
                wasCreateOperation = true;
            }
            
            if (result) {
                // Atomically update the entity
                updateEntityAtomically({ masterEntity: result } as Partial<T>);
                
                // Call onAfterBind to update parent components
                onAfterBind(entity);
                
                // Update local state
                setInputValue(result.name);
                setSelectedEntity(null);
                
                // If this was a create operation, refresh lookup to get the new entity
                if (wasCreateOperation) {
                    console.log(`🔄 ExpEntityBinding: Refreshing lookup after create for: ${result.name}`);
                    try {
                        const lookupResponse = await lookupFunction(result.name, 10);
                        if (lookupResponse.success && lookupResponse.data.length > 0) {
                            const exactMatch = lookupResponse.data.find(
                                lookupEntity => lookupEntity.name.toLowerCase() === result.name.toLowerCase()
                            );
                            if (exactMatch) {
                                console.log(`✅ ExpEntityBinding: Found created entity in lookup: ${exactMatch.name}`);
                                // Note: We don't set selectedEntity here since the entity is now bound
                                // This is just to ensure the entity exists in lookup cache for future operations
                            } else {
                                console.log(`⚠️ ExpEntityBinding: Created entity not found in lookup results`);
                            }
                        }
                    } catch (error) {
                        console.warn('EntityBinding: Failed to refresh lookup after create:', error);
                    }
                }
            }
        } catch (error) {
            console.error("Failed to bind entity:", error);
        } finally {
            setIsBinding(false);
        }
    }, [entity, inputValue, selectedEntity, onBeforeBind, onBindToExisting, onCreateAndBind, onAfterBind, lookupFunction]);

    // Handle unbind operation
    const handleUnbind = useCallback(async () => {
        setIsUnbinding(true);
        try {
            const success = await onUnbind(entity.id);
            if (success) {
                // Keep the name in the input field for easy re-binding
                const masterEntityName = entity.masterEntity?.name || entity.name;
                setInputValue(masterEntityName);
                
                // Atomically update the entity
                updateEntityAtomically({ masterEntity: undefined } as Partial<T>);
                onAfterBind(entity);
                
                // First, check if the name matches any preloaded option
                const matchingPreloadedOption = preloadedOptions.find(
                    option => option.name.toLowerCase() === masterEntityName.toLowerCase()
                );
                
                if (matchingPreloadedOption) {
                    console.log(`✅ ExpEntityBinding: Found matching option in preloaded after unbind: ${matchingPreloadedOption.name}`);
                    setSelectedEntity(matchingPreloadedOption);
                } else {
                    // If not found in preloaded, try lookup
                    console.log(`🔍 ExpEntityBinding: Searching via lookup after unbind for: ${masterEntityName}`);
                    try {
                        const lookupResponse = await lookupFunction(masterEntityName, 10);
                        if (lookupResponse.success && lookupResponse.data.length > 0) {
                            const exactMatch = lookupResponse.data.find(
                                lookupEntity => lookupEntity.name.toLowerCase() === masterEntityName.toLowerCase()
                            );
                            if (exactMatch) {
                                console.log(`✅ ExpEntityBinding: Found matching option via lookup after unbind: ${exactMatch.name}`);
                                setSelectedEntity(exactMatch);
                            } else {
                                console.log(`❌ ExpEntityBinding: No exact match found via lookup after unbind for "${masterEntityName}"`);
                                setSelectedEntity(null);
                            }
                        } else {
                            console.log(`❌ ExpEntityBinding: Lookup returned no results after unbind for "${masterEntityName}"`);
                            setSelectedEntity(null);
                        }
                    } catch (error) {
                        console.warn('EntityBinding: Failed to lookup after unbind:', error);
                        setSelectedEntity(null);
                    }
                }
            }
        } catch (error) {
            console.error("Failed to unbind entity:", error);
        } finally {
            setIsUnbinding(false);
        }
    }, [entity, preloadedOptions, onUnbind, onAfterBind, lookupFunction]);

    // Handle input value change
    const handleInputChange = useCallback((value: string) => {
        setInputValue(value);
        // Clear selected entity when user types manually
        if (selectedEntity && value !== selectedEntity.name) {
            setSelectedEntity(null);
        }
    }, [selectedEntity]);

    // Handle entity selection from autocomplete
    const handleEntitySelect = useCallback((entity: LookupEntity | null) => {
        console.log(`🔄 ExpEntityBinding: handleEntitySelect called with:`, entity?.name || 'null');
        console.log(`🔄 ExpEntityBinding: Previous selectedEntity:`, selectedEntity?.name || 'null');
        setSelectedEntity(entity);
        if (entity) {
            setInputValue(entity.name);
            console.log(`🔄 ExpEntityBinding: Set selectedEntity to:`, entity.name);
        } else {
            console.log(`🔄 ExpEntityBinding: Cleared selectedEntity`);
        }
    }, [selectedEntity]);

    // Render button based on current state
    const renderButton = () => {
        switch (bindingState) {
            case BindingState.BOUND:
                return (
                    <button
                        onClick={handleUnbind}
                        disabled={isUnbinding || disabled}
                        className={`${styles.bindingButton} ${styles.unbindButton}`}
                    >
                        {isUnbinding ? "..." : "Unbind"}
                    </button>
                );
                
            case BindingState.UNBOUND_WITH_MATCH:
                return (
                    <button
                        onClick={handleBind}
                        disabled={isBinding || disabled || !inputValue}
                        className={`${styles.bindingButton} ${styles.linkButton}`}
                        title={`Bind to existing: ${selectedEntity?.name}`}
                    >
                        {isBinding ? "..." : "Link"}
                    </button>
                );
                
            case BindingState.UNBOUND_NO_MATCH:
            default:
                return (
                    <button
                        onClick={handleBind}
                        disabled={isBinding || disabled || !inputValue}
                        className={`${styles.bindingButton} ${styles.createButton}`}
                        title={`Create new: ${inputValue}`}
                    >
                        {isBinding ? "..." : "Create"}
                    </button>
                );
        }
    };

    return (
        <div className={styles.wrapper}>
            {bindingState === BindingState.BOUND ? (
                // Bound state - show readonly input with master entity name
                <div className={styles.wrapper}>
                    <span className={`${commonStyles.muLabel} ${styles.bindingName} ${styles.boundState}`}>
                        {entity.masterEntity?.name}
                    </span>
                    {renderButton()}
                </div>
            ) : (
                // Unbound state - show autocomplete input
                <div className={styles.wrapper}>
                    <DynamicAutocompleteInput
                        value={inputValue}
                        onChange={handleInputChange}
                        onSelect={handleEntitySelect}
                        lookupFunction={lookupFunction}
                        preloadedOptions={preloadedOptions}
                        selectedEntity={selectedEntity}
                        placeholder="Entity name"
                        disabled={disabled}
                        className={`${styles.bindingName} ${
                            bindingState === BindingState.UNBOUND_WITH_MATCH ? styles.matchedState : ''
                        }`}
                    />
                    {renderButton()}
                </div>
            )}
        </div>
    );
};
