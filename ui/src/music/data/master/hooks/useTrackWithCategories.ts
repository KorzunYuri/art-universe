import { useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchTrackWithCategories } from "@/music/data/master/api/music-data-tracks.ts";
import { masterEntitiesKeys } from "@/music/shared/utils/query-keys.ts";

/**
 * Hook for fetching and managing track with categories
 */
export function useTrackWithCategories(trackId: number) {
    const queryClient = useQueryClient();
    const queryKey = masterEntitiesKeys.withRelationsDetail('track', trackId);

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: masterEntitiesKeys.type('track') });
    };

    const trackQuery = useQuery({
        queryKey,
        queryFn: async () => {
            return await fetchTrackWithCategories(trackId);
        }
    });

    return {
        track: trackQuery.data,
        invalidateTrack: invalidate,
        isLoading: trackQuery.isLoading,
        isError: trackQuery.isError,
        error: trackQuery.error,
    };
}
