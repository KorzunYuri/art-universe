import { useMasterEntity } from '@/music/data/master/hooks/useMasterEntity';
import type { Track } from '@/music/shared/types/entities';
import styles from '../TracksTable.module.css';

interface PrimaryArtistCellProps {
    track: Track;
}

export const PrimaryArtistCell = ({ track }: PrimaryArtistCellProps) => {
    const { entity: artist, isLoading } = useMasterEntity('artist', track.primaryArtistId);

    if (isLoading) {
        return <span className={styles.loadingText}>Loading...</span>;
    }

    return (
        <span>{artist?.name ?? `Artist #${track.primaryArtistId}`}</span>
    );
};
