import { useQuery } from "@tanstack/react-query";
import { getQuizBinding } from "@/music-universe/music-quiz/api/music-quiz-common-binding";
import type { MusicQuizSupportedEntityType } from "@/music-universe/music-quiz/types/music-quiz-entity";

/**
 * Query keys for quiz bindings
 */
export const quizBindingKeys = {
    all: ['quizBindings'] as const,
    type: (entityType: MusicQuizSupportedEntityType) =>
        [...quizBindingKeys.all, entityType] as const,
    detail: (entityType: MusicQuizSupportedEntityType, masterId: number) =>
        [...quizBindingKeys.type(entityType), 'detail', masterId] as const,
};

/**
 * Hook for managing quiz binding status
 * 
 * @param entityType The type of entity (artist or track)
 * @param masterId The master entity ID (null if no master entity exists)
 * @returns Object with binding status and utility functions
 */
export function useQuizBinding(
    entityType: MusicQuizSupportedEntityType,
    masterId: number | null
) {
    const quizBindingQuery = useQuery({
        queryKey: masterId ? quizBindingKeys.detail(entityType, masterId) : ['quizBindings', 'null'],
        queryFn: async () => {
            if (!masterId) {
                return { masterId: 0, isBound: false, bindingId: null };
            }
            return await getQuizBinding(entityType, masterId);
        },
        enabled: true
    });

    return {
        isBound: quizBindingQuery.data?.isBound ?? false,
        bindingId: quizBindingQuery.data?.bindingId ?? null,
        isLoading: quizBindingQuery.isLoading,
        isError: quizBindingQuery.isError,
        error: quizBindingQuery.error,
    };
}
