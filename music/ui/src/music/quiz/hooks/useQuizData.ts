import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { musicQuizApi } from '../api/musicQuizApi.ts';
import { quizKeys } from '@/music/quiz/utils/query-keys.ts';
import type { PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';

export const useGames = (page = 0, size = 20) => {
  return useQuery({
    queryKey: quizKeys.gamesList(page, size),
    queryFn: () => musicQuizApi.getAllGames(page, size),
  });
};

export const useGame = (gameId: number) => {
  return useQuery({
    queryKey: quizKeys.game(gameId),
    queryFn: () => musicQuizApi.getGame(gameId),
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
    onSuccess: (_, { gameId }) => {
      queryClient.invalidateQueries({ queryKey: quizKeys.game(gameId) });
    },
  });
};

export const useApproveGeneration = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ generationId }: { generationId: number }) =>
      musicQuizApi.approveGeneration(generationId),
    onSuccess: (_, { generationId }) => {
      // Find the game that contains this generation and invalidate it
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      queryClient.invalidateQueries({ queryKey: quizKeys.all });
    },
  });
};

export const useDisapproveGeneration = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ generationId }: { generationId: number }) =>
      musicQuizApi.disapproveGeneration(generationId),
    onSuccess: (_, { generationId }) => {
      // Find the game that contains this generation and invalidate it
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      queryClient.invalidateQueries({ queryKey: quizKeys.all });
    },
  });
};

export const useDeleteGenerationTrack = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ generationId, trackId }: { generationId: number; trackId: number }) =>
      musicQuizApi.deleteGenerationTrack(generationId, trackId),
    onSuccess: (_, { generationId }) => {
      queryClient.invalidateQueries({ queryKey: quizKeys.generationTracks(generationId) });
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
      queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      // Update game cache with new pipeline
      queryClient.setQueriesData(
        { queryKey: quizKeys.all, predicate: (query) => query.queryKey.includes('game') },
        (oldData: any) => {
          if (oldData?.pipeline?.id === updatedPipeline.id) {
            return { ...oldData, pipeline: updatedPipeline };
          }
          return oldData;
        }
      );
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
      queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      // Update game cache with new pipeline
      queryClient.setQueriesData(
        { queryKey: quizKeys.all, predicate: (query) => query.queryKey.includes('game') },
        (oldData: any) => {
          if (oldData?.pipeline?.id === updatedPipeline.id) {
            return { ...oldData, pipeline: updatedPipeline };
          }
          return oldData;
        }
      );
    },
  });
};

export const useRemoveStep = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ pipelineId, stepId }: { pipelineId: number; stepId: number }) =>
      musicQuizApi.removeStep(pipelineId, stepId),
    onSuccess: (updatedPipeline) => {
      queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      // Update game cache with new pipeline
      queryClient.setQueriesData(
        { queryKey: quizKeys.all, predicate: (query) => query.queryKey.includes('game') },
        (oldData: any) => {
          if (oldData?.pipeline?.id === updatedPipeline.id) {
            return { ...oldData, pipeline: updatedPipeline };
          }
          return oldData;
        }
      );
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
      queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      // Update game cache with new pipeline
      queryClient.setQueriesData(
        { queryKey: quizKeys.all, predicate: (query) => query.queryKey.includes('game') },
        (oldData: any) => {
          if (oldData?.pipeline?.id === updatedPipeline.id) {
            return { ...oldData, pipeline: updatedPipeline };
          }
          return oldData;
        }
      );
    },
  });
};

export const useExecuteStep = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ pipelineId, stepId }: { pipelineId: number; stepId: number }) =>
      musicQuizApi.executeStep(pipelineId, stepId),
    onSuccess: (updatedPipeline) => {
      queryClient.setQueryData(quizKeys.pipeline(updatedPipeline.id), updatedPipeline);
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
      // Update game cache with new pipeline
      queryClient.setQueriesData(
        { queryKey: quizKeys.all, predicate: (query) => query.queryKey.includes('game') },
        (oldData: any) => {
          if (oldData?.pipeline?.id === updatedPipeline.id) {
            return { ...oldData, pipeline: updatedPipeline };
          }
          return oldData;
        }
      );
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
