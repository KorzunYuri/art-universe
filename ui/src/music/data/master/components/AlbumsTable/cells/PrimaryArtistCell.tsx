import { useMasterEntity } from '@/music/data/master/hooks/useMasterEntity';
import type { Album } from '@/music/shared/types/entities';
import styles from '../AlbumsTable.module.css';

interface PrimaryArtistCellProps {
    album: Album;
}

export const PrimaryArtistCell = ({ album }: PrimaryArtistCellProps) => {
    const { entity: artist, isLoading } = useMasterEntity('artist', album.primaryArtistId);

    if (isLoading) {
        return <span className={styles.loadingText}>Loading...</span>;
    }

    return (
        <span>{artist?.name ?? `Artist #${album.primaryArtistId}`}</span>
    );
};
