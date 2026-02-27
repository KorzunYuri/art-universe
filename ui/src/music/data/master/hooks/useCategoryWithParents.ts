import { useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchCategoryWithParents } from "@/music/data/master/api/music-data-categories.ts";
import { masterEntitiesKeys } from "@/music/shared/utils/query-keys.ts";

/**
 * Hook for fetching and managing category with parents
 * 
 * @param categoryId The ID of the category to fetch
 * @returns Object with category data and utility functions
 */
export function useCategoryWithParents(categoryId: number) {
    const queryClient = useQueryClient();
    const queryKey = masterEntitiesKeys.withRelationsDetail('category', categoryId);

    /**
     * Invalidates the category in the query cache, forcing a refetch
     */
    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey });
    };

    const categoryQuery = useQuery({
        queryKey,
        queryFn: async () => {
            console.log(`🔄 Fetching category with parents for ID: ${categoryId}`);
            return await fetchCategoryWithParents(categoryId);
        }
    });

    return {
        category: categoryQuery.data,
        invalidateCategory: invalidate,
        isLoading: categoryQuery.isLoading,
        isError: categoryQuery.isError,
        error: categoryQuery.error,
    };
}
