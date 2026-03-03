import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { musicQuizApi } from '../api/musicQuizApi.ts';
import { quizKeys } from '@/music/quiz/utils/query-keys.ts';
import type { PipelineStepDto, PipelineDto } from '@/music/quiz/types/pipeline-steps.ts';

/**
 * Helper function to update pipeline cache
 * Game and pipeline are cached separately, so we only update the pipeline cache
 */
const updatePipelineCache = (queryClient: any, updatedPipeline: PipelineDto) => {
  queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
};

export const useGames = (page = 0, size = 20) => {
  return useQuery({
    queryKey: quizKeys.gamesList(page, size),
    queryFn: () => musicQuizApi.getAllGames(page, size),
  });
};

export const useGame = (gameId: number) => {
  const queryClient = useQueryClient();

  return useQuery({
    queryKey: quizKeys.game(gameId),
    queryFn: async () => {
      const gameWithPipeline = await musicQuizApi.getGame(gameId);

      // Separate pipeline from game and cache it separately
      if (gameWithPipeline.pipeline) {
        queryClient.setQueryData(
          quizKeys.pipeline(gameWithPipeline.pipeline.id),
          gameWithPipeline.pipeline
        );
      }

      // Return game without pipeline for the game cache
      const { pipeline, ...gameData } = gameWithPipeline;
      return {
        ...gameData,
        pipelineId: pipeline?.id
      };
    },
    enabled: !!gameId,
  });
};

export const useGenerationTracks = (generationId: number) => {
  return useQuery({
    queryKey: quizKeys.generationTracks(generationId),
    queryFn: () => musicQuizApi.getGenerationTracks(generationId),
    enabled: !!generationId,
  });
};

export const useCreateGame = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: musicQuizApi.createGame,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
    },
  });
};

export const useGenerateTracks = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ gameId, steps }: { gameId: number; steps?: any[] }) =>
      musicQuizApi.generateTracks(gameId, steps),
    onSuccess: (newGeneration, { gameId }) => {
      // Add the new generation to the generations list cache
      // Prepend to maintain descending order by createdAt (backend sorts: OrderByCreatedAtDesc)
      queryClient.setQueryData(
        quizKeys.generations(gameId),
        (oldData: any[] | undefined) => {
          if (!oldData) return [newGeneration];
          return [newGeneration, ...oldData];
        }
      );

      // Invalidate the game query since it may have pipeline changes
      // We could avoid this if the backend returns the updated game
      queryClient.invalidateQueries({ queryKey: quizKeys.game(gameId) });
    },
  });
};

export const useApproveGeneration = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ generationId }: { generationId: number }) =>
      musicQuizApi.approveGeneration(generationId),
    onSuccess: (updatedGeneration) => {
      // Update all generations list caches that contain this generation
      queryClient.setQueriesData(
        {
          queryKey: quizKeys.all,
          predicate: (query: any) => {
            // Match queries like ['quiz', 'generations', <gameId>]
            return query.queryKey[0] === 'quiz' &&
                   query.queryKey[1] === 'generations' &&
                   typeof query.queryKey[2] === 'number';
          }
        },
        (oldData: any[] | undefined) => {
          if (!oldData) return oldData;
          return oldData.map(gen =>
            gen.id === updatedGeneration.id ? updatedGeneration : gen
          );
        }
      );
    },
  });
};

export const useDisapproveGeneration = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ generationId }: { generationId: number }) =>
      musicQuizApi.disapproveGeneration(generationId),
    onSuccess: (updatedGeneration) => {
      // Update all generations list caches that contain this generation
      queryClient.setQueriesData(
        {
          queryKey: quizKeys.all,
          predicate: (query: any) => {
            // Match queries like ['quiz', 'generations', <gameId>]
            return query.queryKey[0] === 'quiz' &&
                   query.queryKey[1] === 'generations' &&
                   typeof query.queryKey[2] === 'number';
          }
        },
        (oldData: any[] | undefined) => {
          if (!oldData) return oldData;
          return oldData.map(gen =>
            gen.id === updatedGeneration.id ? updatedGeneration : gen
          );
        }
      );
    },
  });
};

export const useDeleteGenerationTrack = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ generationId, trackId }: { generationId: number; trackId: number }) =>
      musicQuizApi.deleteGenerationTrack(generationId, trackId),
    onSuccess: (_, { generationId, trackId }) => {
      // Optimistically remove the track from the cache instead of invalidating
      queryClient.setQueryData(
        quizKeys.generationTracks(generationId),
        (oldData: any[] | undefined) => {
          if (!oldData) return oldData;
          return oldData.filter(track => track.id !== trackId);
        }
      );
    },
  });
};

// Pipeline management hooks
export const useAddStep = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pipelineId, stepDto, position }: {
      pipelineId: number;
      stepDto: PipelineStepDto;
      position: number;
    }) => musicQuizApi.addStep(pipelineId, stepDto, position),
    onSuccess: (updatedPipeline) => {
      updatePipelineCache(queryClient, updatedPipeline);
    },
  });
};

export const useMoveStep = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pipelineId, stepId, newPosition }: {
      pipelineId: number;
      stepId: number;
      newPosition: number;
    }) => musicQuizApi.moveStep(pipelineId, stepId, newPosition),
    onSuccess: (updatedPipeline) => {
      updatePipelineCache(queryClient, updatedPipeline);
    },
  });
};

export const useRemoveStep = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pipelineId, stepId }: { pipelineId: number; stepId: number }) =>
      musicQuizApi.removeStep(pipelineId, stepId),
    onSuccess: (updatedPipeline) => {
      updatePipelineCache(queryClient, updatedPipeline);
    },
  });
};

export const useUpdateStepConfiguration = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pipelineId, stepId, stepDto }: {
      pipelineId: number;
      stepId: number;
      stepDto: PipelineStepDto;
    }) => musicQuizApi.updateStepConfiguration(pipelineId, stepId, stepDto),
    onSuccess: (updatedPipeline) => {
      updatePipelineCache(queryClient, updatedPipeline);
    },
  });
};

export const useExecuteStep = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pipelineId, stepId }: { pipelineId: number; stepId: number }) =>
      musicQuizApi.executeStep(pipelineId, stepId),
    onSuccess: (updatedPipeline) => {
      updatePipelineCache(queryClient, updatedPipeline);
    },
  });
};

export const useGenerations = (gameId: number) => {
  return useQuery({
    queryKey: quizKeys.generations(gameId),
    queryFn: () => musicQuizApi.getGenerations(gameId),
    enabled: !!gameId,
  });
};

export const useStepPreview = (stepId: number, enabled: boolean = true) => {
  return useQuery({
    queryKey: quizKeys.stepPreview(stepId),
    queryFn: () => musicQuizApi.getStepPreview(stepId),
    enabled: !!stepId && enabled,
  });
};

/**
 * Hook to fetch a pipeline by ID
 * Used for both game pipelines and generation pipelines
 */
export const usePipeline = (pipelineId: number | undefined) => {
  return useQuery({
    queryKey: quizKeys.pipeline(pipelineId!),
    queryFn: () => musicQuizApi.getPipeline(pipelineId!),
    enabled: !!pipelineId,
  });
};

/**
 * @deprecated Use usePipeline instead
 */
export const useGenerationPipeline = usePipeline;
