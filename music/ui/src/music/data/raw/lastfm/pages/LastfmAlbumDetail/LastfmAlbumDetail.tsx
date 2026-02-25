import {useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import {useLastfmEntity} from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import {useLastfmEntityApproval} from '@/music/data/raw/lastfm/hooks/useLastfmEntityApproval';
import {useLastfmAlbumTracks} from '@/music/data/raw/lastfm/hooks/useLastfmAlbumTracks';
import {useApplicableRelationTypes} from '@/music/data/master/hooks/useApplicableRelationTypes';
import {ExternalLink} from '@/music/shared/components';
import {ApprovalToggle, ArtistRelatedEntityBinding, EntityTagPanel} from '@/music/data/raw/lastfm/components';
import {LastfmConfig} from '@/music/data/raw/lastfm/config/lastfmconfig';
import {getMasterEntityUrl} from '@/music/data/master/utils/masterEntityUrl';
import {
    type AlbumTrackBindItem,
    bindAlbumWithTracklist,
} from '@/music/data/master/api/music-data-common-binding';
import {useQueryClient} from '@tanstack/react-query';
import {lastfmAlbumTracksKeys} from '@/music/data/raw/lastfm/utils/query-keys';
import {rawEntitiesKeys} from '@/music/shared/utils/query-keys';
import type {LastfmAlbumTrackDto} from '@/music/data/raw/lastfm/api/lastfm-albums';
import styles from '../LastfmDetailPage.module.scss';

interface TrackEditState {
    trackOrder: number;
    relationTypeId: number | undefined;
}


export const LastfmAlbumDetail = () => {
    const { albumId } = useParams<{ albumId: string }>();
    const id = Number(albumId);
    const queryClient = useQueryClient();

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useLastfmEntity('album', id);

    const {
        isApproving,
        setApprovalStatus,
        ensureIsValidForBinding,
    } = useLastfmEntityApproval(entity, 'lastfm', updateEntity);

    const { tracks, isLoading: isTracksLoading } = useLastfmAlbumTracks(id);
    const albumArtistId = entity?.artist?.id;

    // Relation types for ALBUM→TRACK
    const { relationTypes } = useApplicableRelationTypes('album', 'track');

    // Master artist selection state (from album-level ArtistRelatedEntityBinding)
    const [masterArtistId, setMasterArtistId] = useState<number | null>(null);

    // User-editable per-track state (order and relation type)
    const [trackEditStates, setTrackEditStates] = useState<Record<number, TrackEditState>>({});

    // Per-track binding selection state (artist + master entity, updated via callbacks)
    const [trackSelections, setTrackSelections] = useState<Record<number, { artistId: number | null; masterEntityId: number | null }>>({});

    // Binding in progress
    const [isBindingWithTracks, setIsBindingWithTracks] = useState(false);

    // Initialize per-track states when tracks load
    useEffect(() => {
        if (tracks.length === 0) return;

        const initialEditStates: Record<number, TrackEditState> = {};
        const initialSelections: Record<number, { artistId: number | null; masterEntityId: number | null }> = {};
        tracks.forEach((track, index) => {
            initialEditStates[track.trackId] = {
                trackOrder: track.position ?? index + 1,
                relationTypeId: undefined,
            };
            initialSelections[track.trackId] = { artistId: null, masterEntityId: null };
        });
        setTrackEditStates(initialEditStates);
        setTrackSelections(initialSelections);
    }, [tracks]);

    // Update track order
    const handleTrackOrderChange = useCallback((trackId: number, order: number) => {
        setTrackEditStates(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], trackOrder: order },
        }));
    }, []);

    // Update track relation type
    const handleRelationTypeChange = useCallback((trackId: number, relationTypeId: number | undefined) => {
        setTrackEditStates(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], relationTypeId },
        }));
    }, []);

    // Callbacks fed by per-track ArtistRelatedEntityBinding
    const handleTrackArtistChange = useCallback((trackId: number, artistId: number | null) => {
        setTrackSelections(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], artistId },
        }));
    }, []);

    const handleTrackEntityChange = useCallback((trackId: number, entityId: number | null) => {
        setTrackSelections(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], masterEntityId: entityId },
        }));
    }, []);

    // Can bind with tracklist?
    const canBindWithTracklist = useMemo(() => {
        return masterArtistId != null && tracks.length > 0 && !isBindingWithTracks;
    }, [masterArtistId, tracks.length, isBindingWithTracks]);

    // Handle bind with tracklist
    const handleBindWithTracklist = useCallback(async () => {
        if (!entity || !masterArtistId || tracks.length === 0) return;

        setIsBindingWithTracks(true);
        try {
            const trackItems: AlbumTrackBindItem[] = tracks.map((track) => {
                const editState = trackEditStates[track.trackId];
                const selection = trackSelections[track.trackId];
                const item: AlbumTrackBindItem = {
                    externalTrackId: track.trackId,
                    trackOrder: editState?.trackOrder ?? 1,
                    trackName: track.trackName,
                };

                if (selection?.masterEntityId) {
                    item.masterTrackId = selection.masterEntityId;
                }

                if (editState?.relationTypeId) {
                    item.relationTypeId = editState.relationTypeId;
                }

                if (selection?.artistId) {
                    item.masterPrimaryArtistId = selection.artistId;
                }

                return item;
            });

            const masterAlbumId = entity.getMasterEntity()?.id;

            await bindAlbumWithTracklist('lastfm', id, {
                albumName: masterAlbumId ? undefined : entity.name,
                masterAlbumId: masterAlbumId ?? undefined,
                masterPrimaryArtistId: masterArtistId,
                tracks: trackItems,
            });

            // Invalidate — React Query re-fetches both the tracklist and all track entities
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: lastfmAlbumTracksKeys.detail(id) }),
                queryClient.invalidateQueries({ queryKey: rawEntitiesKeys.type('lastfm', 'track') }),
            ]);
            invalidateEntity();
        } catch (error) {
            console.error('Failed to bind album with tracklist:', error);
        } finally {
            setIsBindingWithTracks(false);
        }
    }, [entity, masterArtistId, tracks, trackEditStates, trackSelections, id, queryClient, invalidateEntity]);

    if (isLoading) {
        return <div className={styles.loading}>Loading album...</div>;
    }

    if (isError || !entity) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Album not found'}
            </div>
        );
    }

    return (
        <div className={styles.detail}>
            {/* Header: name + external links */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                {entity.artist && (
                    <span className={styles.artistName}>by {entity.artist.name}</span>
                )}
                <div className={styles.externalLinks}>
                    {entity.url && <ExternalLink href={entity.url} label="Last.fm" />}
                    {entity.mbid && (
                        <ExternalLink
                            href={`${LastfmConfig.mbBaseUrls.album}${entity.mbid}`}
                            label="MusicBrainz"
                        />
                    )}
                </div>
            </div>

            {/* Attributes */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Stats</h3>
                <div className={styles.attrRow}>
                    <span className={styles.attrLabel}>Play count</span>
                    <span className={styles.attrValue}>{entity.playCount?.toLocaleString() ?? '—'}</span>
                </div>
                <div className={styles.attrRow}>
                    <span className={styles.attrLabel}>Listeners</span>
                    <span className={styles.attrValue}>{entity.listenersCount?.toLocaleString() ?? '—'}</span>
                </div>
            </section>

            {/* Tracklist */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>
                    Tracklist{tracks.length > 0 && ` (${tracks.length})`}
                </h3>
                {isTracksLoading ? (
                    <div className={styles.loading}>Loading tracks...</div>
                ) : tracks.length === 0 ? (
                    <div style={{ fontSize: '0.85rem', color: '#94a3b8' }}>No tracks</div>
                ) : (
                    <table className={styles.tracklistTable}>
                        <thead>
                            <tr>
                                <th className={styles.positionCol}>#</th>
                                <th>Name</th>
                                <th className={styles.artistCol}>Artist</th>
                                <th className={styles.orderCol}>Order</th>
                                <th className={styles.typeCol}>Type</th>
                                <th className={styles.bindingCol}>Binding</th>
                            </tr>
                        </thead>
                        <tbody>
                            {tracks.map((track, index) => (
                                <TrackRow
                                    key={track.trackId}
                                    track={track}
                                    index={index}
                                    albumArtistId={albumArtistId}
                                    editState={trackEditStates[track.trackId]}
                                    relationTypes={relationTypes}
                                    onOrderChange={handleTrackOrderChange}
                                    onRelationTypeChange={handleRelationTypeChange}
                                    onMasterArtistChange={handleTrackArtistChange}
                                    onMasterEntityChange={handleTrackEntityChange}
                                />
                            ))}
                        </tbody>
                    </table>
                )}
            </section>

            {/* Approval */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Approval</h3>
                <div className={styles.controlRow}>
                    <ApprovalToggle
                        status={entity.approvalStatus}
                        onChange={setApprovalStatus}
                        disabled={isApproving}
                    />
                </div>
            </section>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <ArtistRelatedEntityBinding
                        dataSource="lastfm"
                        entityType="album"
                        entityId={id}
                        onBeforeBind={ensureIsValidForBinding}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        onMasterArtistChange={setMasterArtistId}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('album', masterId)}
                    />
                </div>

                {/* Bind with tracklist button */}
                {tracks.length > 0 && (
                    <div className={styles.bindWithTracksRow}>
                        <button
                            onClick={handleBindWithTracklist}
                            disabled={!canBindWithTracklist}
                            className={styles.bindWithTracksButton}
                        >
                            {isBindingWithTracks ? 'Binding...' : 'Bind with tracklist'}
                        </button>
                        {!masterArtistId && tracks.length > 0 && (
                            <span className={styles.bindHint}>Select master artist first</span>
                        )}
                    </div>
                )}
            </section>

            {/* Tags */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Tags</h3>
                <EntityTagPanel
                    entityType="album"
                    entityId={entity.id}
                    entityApprovalStatus={entity.approvalStatus}
                    tagPageBaseUrl="/music/data/raw/lastfm/tags/"
                />
            </section>
        </div>
    );
};

// --- Track row component ---

interface TrackRowProps {
    track: LastfmAlbumTrackDto;
    index: number;
    albumArtistId: number | undefined;
    editState: TrackEditState | undefined;
    relationTypes: { id: number; name: string }[];
    onOrderChange: (trackId: number, order: number) => void;
    onRelationTypeChange: (trackId: number, relationTypeId: number | undefined) => void;
    onMasterArtistChange: (trackId: number, artistId: number | null) => void;
    onMasterEntityChange: (trackId: number, entityId: number | null) => void;
}

const TrackRow = ({
    track,
    index,
    albumArtistId,
    editState,
    relationTypes,
    onOrderChange,
    onRelationTypeChange,
    onMasterArtistChange,
    onMasterEntityChange,
}: TrackRowProps) => {
    const { entity, updateEntity, invalidateEntity } = useLastfmEntity('track', track.trackId);
    const { ensureIsValidForBinding } = useLastfmEntityApproval(entity, 'lastfm', updateEntity);
    const linkToMasterUrl = useCallback((masterId: number) => getMasterEntityUrl('track', masterId), []);

    const handleArtistChange = useCallback(
        (artistId: number | null) => onMasterArtistChange(track.trackId, artistId),
        [track.trackId, onMasterArtistChange]
    );
    const handleEntityChange = useCallback(
        (entityId: number | null) => onMasterEntityChange(track.trackId, entityId),
        [track.trackId, onMasterEntityChange]
    );

    return (
        <tr>
            <td className={styles.positionCol}>
                {track.position ?? index + 1}
            </td>
            <td>
                <Link
                    to={`/music/data/raw/lastfm/tracks/${track.trackId}`}
                    className={styles.trackLink}
                >
                    {track.trackName}
                </Link>
            </td>
            <td className={styles.artistCol}>
                {track.trackArtistId !== albumArtistId
                    ? track.trackArtistName
                    : ''}
            </td>
            <td className={styles.orderCol}>
                <input
                    type="number"
                    min={1}
                    value={editState?.trackOrder ?? index + 1}
                    onChange={(e) => onOrderChange(track.trackId, parseInt(e.target.value) || 1)}
                    className={styles.orderInput}
                />
            </td>
            <td className={styles.typeCol}>
                <select
                    value={editState?.relationTypeId ?? ''}
                    onChange={(e) => onRelationTypeChange(
                        track.trackId,
                        e.target.value ? Number(e.target.value) : undefined
                    )}
                    className={styles.typeSelect}
                >
                    <option value="">Default</option>
                    {relationTypes.map(rt => (
                        <option key={rt.id} value={rt.id}>{rt.name}</option>
                    ))}
                </select>
            </td>
            <td className={styles.bindingCol}>
                <ArtistRelatedEntityBinding
                    dataSource="lastfm"
                    entityType="track"
                    entityId={track.trackId}
                    onBeforeBind={ensureIsValidForBinding}
                    onAfterBind={invalidateEntity}
                    onAfterUnbind={invalidateEntity}
                    linkToMasterUrl={linkToMasterUrl}
                    compact
                    className={styles.trackBinding}
                    onMasterArtistChange={handleArtistChange}
                    onMasterEntityChange={handleEntityChange}
                />
            </td>
        </tr>
    );
};
