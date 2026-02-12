import { useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchAlbumWithCategories } from "@/music/data/master/api/music-data-albums.ts";
import { masterEntitiesKeys } from "@/music/shared/utils/query-keys.ts";

/**
 * Hook for fetching and managing album with categories
 */
export function useAlbumWithCategories(albumId: number) {
    const queryClient = useQueryClient();
    const queryKey = masterEntitiesKeys.withRelationsDetail('album', albumId);

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey });
    };

    const albumQuery = useQuery({
        queryKey,
        queryFn: async () => {
            return await fetchAlbumWithCategories(albumId);
        }
    });

    return {
        album: albumQuery.data,
        invalidateAlbum: invalidate,
        isLoading: albumQuery.isLoading,
        isError: albumQuery.isError,
        error: albumQuery.error,
    };
}
