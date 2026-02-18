import { useCallback, useEffect, useState } from "react";
import { useLastfmEntity } from "@/music/data/raw/lastfm/hooks/useLastfmEntity.tsx";
import { EntityLookup } from "@/music/shared/components";
import { EntityBinding, type EntityBindingProps } from "@/music/data/raw/lastfm/components/EntityBinding/EntityBinding.tsx";
import type { LookupEntity } from "@/music/shared/types/lookup.ts";
import type { MasterEntityType } from "@/music/shared/types/entities.ts";
import type { LastfmSupportedEntityType } from "@/music/data/raw/lastfm/types/lastfm-entity.ts";
import type { ArtistRelatedLookupContext } from "@/music/data/raw/shared/types/lookup-context.ts";
import { LookupContextFactory } from "@/music/shared/types/lookup-context.ts";
import { useMasterEntity } from "@/music/data/master/hooks/useMasterEntity.ts";
import styles from "./ArtistRelatedEntityBinding.module.scss";

type ArtistRelatedEntityType = "album" | "track";

interface ArtistRelatedEntityBindingProps<K extends MasterEntityType & ArtistRelatedEntityType>
    extends Omit<EntityBindingProps<K>, 'overrideLookupContext' | 'overrideMasterArtistId'> {
    /** Notifies parent when master artist selection changes */
    onMasterArtistChange?: (masterArtistId: number | null) => void;
    /** Renders artist picker and entity binding side-by-side (for table rows) */
    compact?: boolean;
    /** Fires whenever the effective master entity (bound or matched) changes */
    onMasterEntityChange?: (entityId: number | null) => void;
    /** Extra class applied to the wrapper div */
    className?: string;
}

/**
 * Wraps EntityBinding with an artist picker for album/track entities.
 * The artist picker controls which master artist is used for lookup filtering and binding.
 */
export const ArtistRelatedEntityBinding = <T extends LastfmSupportedEntityType & ArtistRelatedEntityType>({
    dataSource,
    entityType,
    entityId,
    onMasterArtistChange,
    disabled = false,
    compact = false,
    onMasterEntityChange,
    className,
    ...entityBindingProps
}: ArtistRelatedEntityBindingProps<T>) => {

    const {
        entity,
        isLoading: isEntityLoading,
    } = useLastfmEntity<T>(entityType, entityId);

    // Artist picker state
    const [artistSearchString, setArtistSearchString] = useState('');
    const [selectedArtist, setSelectedArtist] = useState<LookupEntity | null>(null);
    const [prevEntityId, setPrevEntityId] = useState(entityId);

    // Reset state when switching entities
    if (prevEntityId !== entityId) {
        setPrevEntityId(entityId);
        setArtistSearchString('');
        setSelectedArtist(null);
    }

    // When entity is bound, fetch the master artist to pre-populate the artist picker
    const boundMasterArtistId = entity?.getMasterArtistId?.() ?? 0;
    const { entity: masterArtist } = useMasterEntity('artist', boundMasterArtistId);

    // Pre-fill artist from entity data
    useEffect(() => {
        if (!entity) return;

        if (boundMasterArtistId > 0 && masterArtist) {
            // Entity is bound and master artist loaded — pre-select the master artist directly
            const lookupEntity: LookupEntity = { id: masterArtist.id, name: masterArtist.name };
            setSelectedArtist(lookupEntity);
            setArtistSearchString(masterArtist.name);
            onMasterArtistChange?.(masterArtist.id);
        } else if (boundMasterArtistId === 0) {
            // Entity not bound — use external artist name as search string for auto-select
            setArtistSearchString(entity.artist?.name ?? '');
        }
    }, [entity, masterArtist, boundMasterArtistId, onMasterArtistChange]);

    // Handle artist selection
    const handleArtistSelect = useCallback((artist: LookupEntity | null) => {
        setSelectedArtist(artist);
        if (artist) {
            setArtistSearchString(artist.name);
        }
        onMasterArtistChange?.(artist?.id ?? null);
    }, [onMasterArtistChange]);

    // Handle artist search input change
    const handleArtistInputChange = useCallback((value: string) => {
        setArtistSearchString(value);
        if (selectedArtist && value !== selectedArtist.name) {
            setSelectedArtist(null);
            onMasterArtistChange?.(null);
        }
    }, [selectedArtist, onMasterArtistChange]);

    // Build override lookup context using selected master artist
    const overrideLookupContext: ArtistRelatedLookupContext | undefined = selectedArtist
        ? {
            type: 'artist-related',
            masterArtistId: selectedArtist.id,
            dataSource: dataSource,
            externalArtistId: entity?.getExternalArtistId?.(),
        }
        : undefined;

    const handleMasterEntityChange = useCallback(
        (entity: LookupEntity | null) => onMasterEntityChange?.(entity?.id ?? null),
        [onMasterEntityChange]
    );

    const isEntityDisabled = disabled || !selectedArtist;

    if (isEntityLoading) {
        return <div style={{ fontSize: '0.85rem', color: '#94a3b8' }}>Loading...</div>;
    }

    return (
        <div className={`${compact ? styles.wrapperCompact : styles.wrapper}${className ? ` ${className}` : ''}`}>
            {/* Artist row */}
            <div className={styles.row}>
                <span className={styles.label}>Artist</span>
                <div className={styles.artistLookup}>
                    <EntityLookup
                        dataSource="master"
                        entityType="artist"
                        searchString={artistSearchString}
                        context={LookupContextFactory.basic()}
                        onSelect={handleArtistSelect}
                        onChange={handleArtistInputChange}
                        selectedEntity={selectedArtist}
                        placeholder="Select master artist"
                        disabled={disabled}
                        autoSelectExactMatch={true}
                        limit={10}
                    />
                </div>
            </div>

            {/* Entity row (EntityBinding) */}
            <div className={styles.row}>
                <span className={styles.label}>
                    {entityType === 'album' ? 'Album' : 'Track'}
                </span>
                <div style={{ flex: 1, minWidth: 0 }}>
                    <EntityBinding
                        dataSource={dataSource}
                        entityType={entityType}
                        entityId={entityId}
                        disabled={isEntityDisabled}
                        overrideLookupContext={overrideLookupContext}
                        overrideMasterArtistId={selectedArtist?.id}
                        onSelectedEntityChange={onMasterEntityChange ? handleMasterEntityChange : undefined}
                        {...entityBindingProps}
                    />
                </div>
            </div>
        </div>
    );
};
