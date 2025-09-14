import { useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchArtistWithCategories } from "@/music-universe/music-data/api/music-data-artists";

/**
 * Hook for fetching and managing artist with categories
 */
export function useArtistWithCategories(artistId: number) {
    const queryClient = useQueryClient();
    const queryKey = ['artistWithCategories', artistId];

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey });
    };

    const artistQuery = useQuery({
        queryKey,
        queryFn: async () => {
            return await fetchArtistWithCategories(artistId);
        }
    });

    return {
        artist: artistQuery.data,
        invalidateArtist: invalidate,
        isLoading: artistQuery.isLoading,
        isError: artistQuery.isError,
        error: artistQuery.error,
    };
}
