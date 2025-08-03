// hooks
import {useCallback, useEffect, useState} from "react";
import {useLastfmEntity} from "@/music-universe/sources/lastfm/hooks/useLastfmEntity.tsx";
// components
import {MasterEntityLookup} from "@/music-universe/shared/components";
// types
import type {
    LookupEntity,
    LookupRequestSourceParams
} from "@/music-universe/music-data/types/master-entities-lookup.ts";
// styles
import commonStyles from "@/music-universe/shared/styles/common.module.scss";
import styles from "./EntityBinding.module.scss";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {
    bindRawEntityToExistingMaster,
    bindRawEntityToNewMaster,
    unbindRawEntity,
    type BoundEntityInfo,
} from "@/music-universe/music-data/api/music-data-binding.ts";
import type {LastfmSupportedEntityType} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";
import {useQueryClient} from "@tanstack/react-query";
import {masterEntityLookupKeys} from "@/music-universe/shared/utils/query-keys.ts";

interface EntityBindingProps<K extends MasterEntityType> {
    dataSource: DataSource;
    entityType: K;
    entityId: number;
    onBeforeBind?: (hasMasterExisted: boolean) => Promise<boolean>;
    onAfterBind?: (hasMasterExisted: boolean) => void;
    onBeforeUnbind?: () => Promise<boolean>;
    onAfterUnbind?: () => void;
    disabled?: boolean;
}

enum BindingState {
    UNBOUND_NO_MATCH = 'unbound_no_match',     // Create button
    UNBOUND_WITH_MATCH = 'unbound_with_match', // Link button  
    BOUND = 'bound'                            // Unbind button
}

/**
 * A component for binding raw ('external') entities to master entities.
 */
export const EntityBinding = <T extends LastfmSupportedEntityType>(
    {
        dataSource,
        entityType,
        entityId,
        onBeforeBind = async (hasMasterExisted: boolean) => true,
        onAfterBind = (hasMasterExisted: boolean) => {},
        onBeforeUnbind = async () => true,
        onAfterUnbind = () => {},
        disabled = false
    }: EntityBindingProps<T>) => {

    const queryClient = useQueryClient();

    const {
        entity,
        isLoading,
        isError
    } = useLastfmEntity<T>(entityType, entityId);

    const [inputValue, setInputValue] = useState('');
    const [isBinding, setIsBinding] = useState(false);
    const [isUnbinding, setIsUnbinding] = useState(false);
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);

    // Initialize input value when entity loads or changes
    useEffect(() => {
        if (entity) {
            setInputValue(entity.masterEntity?.name || entity.name || '');
        }
    }, [entity]);

    const isDisabled = disabled || isLoading || isBinding || isUnbinding || isError;

    const createLookupRequest: (searchString: string) => LookupRequestSourceParams<T> = useCallback((searchString: string) => {
        const baseParams = { search: searchString };
        if (!entity) {
            return baseParams as LookupRequestSourceParams<T>;
        }

        // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
        return {
            ...baseParams,
            rawEntity: entity
        } as LookupRequestSourceParams<T>;
    }, [entity]);

    // Handle input change in autocomplete
    const handleInputChange = useCallback((value: string) => {
        setInputValue(value);
        // Clear selected entity when user types manually
        if (selectedEntity && value !== selectedEntity.name) {
            setSelectedEntity(null);
        }
    }, [selectedEntity]);

    // Handle entity selection from autocomplete
    const handleEntitySelect = useCallback((matchedMasterEntity: LookupEntity | null) => {
        console.log(
            `EntityBinding: handleEntitySelect called with: ${matchedMasterEntity?.name || 'null'} `,
            `Previous selectedEntity: ${selectedEntity?.name || 'null'}`
        );

        if (matchedMasterEntity?.id === selectedEntity?.id) {
            console.log(`🔄 EntityBinding: Skipping update - same entity selected`);
            return;
        }

        setSelectedEntity(matchedMasterEntity);
        if (matchedMasterEntity) {
            setInputValue(matchedMasterEntity.name);
            console.log(`🔄 EntityBinding: Set selectedEntity to:`, matchedMasterEntity.name);
        } else {
            console.log(`🔄 EntityBinding: Cleared selectedEntity`);
        }
    }, [selectedEntity]);

    // Handle loading and error states
    if (isLoading) {
        return <div className={styles.loadingState}>Loading...</div>;
    }

    if (isError) {
        return <div className={styles.errorState}>Error loading entity</div>;
    }

    // Handle case when entity is undefined
    if (!entity) {
        return <div className={styles.emptyState}>No entity data</div>;
    }

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

    // Handle binding operations
    const handleBind = async () => {
        if (!inputValue || !entity) return;

        setIsBinding(true);
        try {
            // Execute pre-bind action
            const wasCreationRequest = !selectedEntity;
            const canProceed = await onBeforeBind(wasCreationRequest);

            if (!canProceed) {
                return;
            }

            let result: BoundEntityInfo<T> | null = null;

            // If an entity was selected from autocomplete, bind to existing
            if (selectedEntity) {
                // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
                result = await bindRawEntityToExistingMaster(selectedEntity.id, entity);
            }
            // Otherwise, create new entity and bind
            else {
                // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
                result = await bindRawEntityToNewMaster<T>(inputValue, entity);
            }

            // Call onAfterBind to update parent components
            onAfterBind(wasCreationRequest);

            // Update local state
            setInputValue(result?.masterEntity.name || '');
            setSelectedEntity(null);

            // If this was a create operation, refresh lookup to get the new entity
            if (wasCreationRequest) {
                console.log(`🔄 EntityBinding: Refreshing lookup after create`);
                // TODO move to onAfterBind on parent side?
                queryClient.invalidateQueries({ queryKey: masterEntityLookupKeys.type(entityType) });
            }
        } catch (error) {
            console.error("Failed to bind entity:", error);
        } finally {
            setIsBinding(false);
        }
    }

    // Handle unbind operation
    const handleUnbind = async () => {
        if (!entity) return;

        setIsUnbinding(true);
        try {
            const canProceed = await onBeforeUnbind();
            if (!canProceed) {
                return;
            }

            const success = await unbindRawEntity(dataSource, entityType, entity.id);
            if (success) {
                // Keep the name in the input field for easy re-binding
                const masterEntityName = entity.masterEntity?.name || entity.name;
                setInputValue(masterEntityName);
                onAfterUnbind();
            }
        } catch (error) {
            console.error("Failed to unbind entity:", error);
        } finally {
            setIsUnbinding(false);
        }
    }

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
                // Bound state
                <>
                <span className={`${commonStyles.muLabel} ${styles.bindingName} ${styles.boundState}`}>
                    {entity.masterEntity?.name}
                </span>
                    {renderButton()}
                </>
            ) : (
                // Unbound state
                <>
                    <MasterEntityLookup
                        searchString={inputValue}
                        entityType={entityType}
                        requestFactory={createLookupRequest}
                        onSelect={handleEntitySelect}
                        onChange={handleInputChange}
                        selectedEntity={selectedEntity}
                        placeholder="Entity name"
                        disabled={isDisabled}
                        className={`${styles.bindingName} ${
                            bindingState === BindingState.UNBOUND_WITH_MATCH ? styles.matchedState : ''
                        }`}
                        autoSelectExactMatch={true}
                    />
                    {renderButton()}
                </>
            )}
        </div>
    );
};
