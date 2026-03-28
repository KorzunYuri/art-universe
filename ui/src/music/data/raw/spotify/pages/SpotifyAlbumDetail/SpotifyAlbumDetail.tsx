import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useSpotifyEntity } from '@/music/data/raw/spotify/hooks/useSpotifyEntity';
import { useSpotifyAlbumTracks } from '@/music/data/raw/spotify/hooks/useSpotifyAlbumTracks';
import { useApplicableRelationTypes } from '@/music/data/master/hooks/useApplicableRelationTypes';
import { ExternalLink } from '@/shared/components';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { ArtistRelatedEntityBinding } from '@/music/data/raw/shared/components';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import {
    type AlbumTrackBindItem,
    bindAlbumWithTracklist,
} from '@/music/data/master/api/music-data-common-binding';
import { useQueryClient } from '@tanstack/react-query';
import { spotifyAlbumTracksKeys } from '@/music/data/raw/spotify/utils/query-keys';
import { rawEntitiesKeys } from '@/music/shared/utils/query-keys';
import type { SpotifyAlbumTrackDto } from '@/music/data/raw/spotify/api/spotify-albums';
import styles from '@/music/data/raw/spotify/styles/SpotifyDetailPage.module.scss';

function formatDuration(ms: number | null): string {
    if (ms === null) return '';
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

interface TrackEditState {
    trackOrder: number;
    relationTypeId: number | undefined;
}

export const SpotifyAlbumDetail = () => {
    const { albumId } = useParams<{ albumId: string }>();
    const id = Number(albumId);
    const queryClient = useQueryClient();

    const {
        entity,
        invalidateEntity,
        isLoading,
        isError,
        error,
    } = useSpotifyEntity('album', id);

    const permissions = usePermissions();
    const curationReadOnly = permissions.spotifyCurationAccess !== 'full';

    const { tracks, isLoading: isTracksLoading } = useSpotifyAlbumTracks(id);

    const { relationTypes } = useApplicableRelationTypes('album', 'track');

    const [masterArtistId, setMasterArtistId] = useState<number | null>(null);
    const [trackEditStates, setTrackEditStates] = useState<Record<number, TrackEditState>>({});
    const [trackSelections, setTrackSelections] = useState<Record<number, { artistId: number | null; masterEntityId: number | null }>>({});
    const [isBindingWithTracks, setIsBindingWithTracks] = useState(false);

    useEffect(() => {
        if (tracks.length === 0) return;

        const initialEditStates: Record<number, TrackEditState> = {};
        const initialSelections: Record<number, { artistId: number | null; masterEntityId: number | null }> = {};
        tracks.forEach((track, index) => {
            initialEditStates[track.trackId] = {
                trackOrder: track.trackNumber ?? index + 1,
                relationTypeId: undefined,
            };
            initialSelections[track.trackId] = { artistId: null, masterEntityId: null };
        });
        setTrackEditStates(initialEditStates);
        setTrackSelections(initialSelections);
    }, [tracks]);

    const handleTrackOrderChange = useCallback((trackId: number, order: number) => {
        setTrackEditStates(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], trackOrder: order },
        }));
    }, []);

    const handleRelationTypeChange = useCallback((trackId: number, relationTypeId: number | undefined) => {
        setTrackEditStates(prev => ({
            ...prev,
            [trackId]: { ...prev[trackId], relationTypeId },
        }));
    }, []);

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

    const canBindWithTracklist = useMemo(() => {
        return masterArtistId != null && tracks.length > 0 && !isBindingWithTracks;
    }, [masterArtistId, tracks.length, isBindingWithTracks]);

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

            await bindAlbumWithTracklist('spotify', id, {
                albumName: masterAlbumId ? undefined : entity.name,
                masterAlbumId: masterAlbumId ?? undefined,
                masterPrimaryArtistId: masterArtistId,
                tracks: trackItems,
            });

            await Promise.all([
                queryClient.invalidateQueries({ queryKey: spotifyAlbumTracksKeys.detail(id) }),
                queryClient.invalidateQueries({ queryKey: rawEntitiesKeys.type('spotify', 'track') }),
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

    const spotifyUrl = entity.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.album}${entity.spotifyId}`;

    return (
        <div className={styles.detail}>
            {/* Header */}
            <div className={styles.header}>
                <h3 className={styles.entityName}>{entity.name}</h3>
                {entity.primaryArtistName && (
                    <span className={styles.artistName}>
                        by {entity.primaryArtistId
                            ? <Link to={`/music/data/raw/spotify/artists/${entity.primaryArtistId}`}>{entity.primaryArtistName}</Link>
                            : entity.primaryArtistName}
                    </span>
                )}
                <div className={styles.externalLinks}>
                    <ExternalLink href={spotifyUrl} label="Spotify" />
                </div>
            </div>

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
                                <th className={styles.durationCol}>Duration</th>
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
                                    editState={trackEditStates[track.trackId]}
                                    relationTypes={relationTypes}
                                    onOrderChange={handleTrackOrderChange}
                                    onRelationTypeChange={handleRelationTypeChange}
                                    onMasterArtistChange={handleTrackArtistChange}
                                    onMasterEntityChange={handleTrackEntityChange}
                                    readOnly={curationReadOnly}
                                />
                            ))}
                        </tbody>
                    </table>
                )}
            </section>

            {/* Master Binding */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Master Binding</h3>
                <div className={styles.bindingWrapper}>
                    <ArtistRelatedEntityBinding
                        dataSource="spotify"
                        entityType="album"
                        entityId={id}
                        onAfterBind={invalidateEntity}
                        onAfterUnbind={invalidateEntity}
                        onMasterArtistChange={setMasterArtistId}
                        linkToMasterUrl={(masterId) => getMasterEntityUrl('album', masterId)}
                        readOnly={curationReadOnly}
                    />
                </div>

                {tracks.length > 0 && (
                    <div className={styles.bindWithTracksRow}>
                        <button
                            onClick={handleBindWithTracklist}
                            disabled={!canBindWithTracklist || curationReadOnly}
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
        </div>
    );
};

// --- Track row component ---

interface TrackRowProps {
    track: SpotifyAlbumTrackDto;
    index: number;
    editState: TrackEditState | undefined;
    relationTypes: { id: number; name: string }[];
    onOrderChange: (trackId: number, order: number) => void;
    onRelationTypeChange: (trackId: number, relationTypeId: number | undefined) => void;
    onMasterArtistChange: (trackId: number, artistId: number | null) => void;
    onMasterEntityChange: (trackId: number, entityId: number | null) => void;
    readOnly: boolean;
}

const TrackRow = ({
    track,
    index,
    editState,
    relationTypes,
    onOrderChange,
    onRelationTypeChange,
    onMasterArtistChange,
    onMasterEntityChange,
    readOnly,
}: TrackRowProps) => {
    const { invalidateEntity } = useSpotifyEntity('track', track.trackId);
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
                {track.trackNumber ?? index + 1}
            </td>
            <td>
                <Link
                    to={`/music/data/raw/spotify/tracks/${track.trackId}`}
                    className={styles.trackLink}
                >
                    {track.trackName}
                </Link>
            </td>
            <td className={styles.durationCol}>
                {formatDuration(track.durationMs)}
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
                    dataSource="spotify"
                    entityType="track"
                    entityId={track.trackId}
                    onAfterBind={invalidateEntity}
                    onAfterUnbind={invalidateEntity}
                    linkToMasterUrl={linkToMasterUrl}
                    compact
                    className={styles.trackBinding}
                    onMasterArtistChange={handleArtistChange}
                    onMasterEntityChange={handleEntityChange}
                    readOnly={readOnly}
                />
            </td>
        </tr>
    );
};
