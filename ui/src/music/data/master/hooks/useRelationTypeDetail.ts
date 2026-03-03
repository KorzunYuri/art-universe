import { useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchRelationTypeWithApplicabilities } from '@/music/data/master/api/music-data-relation-types';
import { relationTypeKeys } from '@/music/shared/utils/query-keys';

export function useRelationTypeDetail(id: number) {
    const queryClient = useQueryClient();
    const queryKey = relationTypeKeys.detail(id);

    const query = useQuery({
        queryKey,
        queryFn: () => fetchRelationTypeWithApplicabilities(id),
    });

    const invalidate = () =>
        queryClient.invalidateQueries({ queryKey: relationTypeKeys.all });

    return {
        relationType: query.data,
        invalidateRelationType: invalidate,
        isLoading: query.isLoading,
        isError: query.isError,
        error: query.error,
    };
}
