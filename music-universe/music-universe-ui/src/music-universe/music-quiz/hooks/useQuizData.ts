import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { musicQuizApi } from '../api/musicQuizApi.ts';
import { quizKeys } from '@/music-universe/music-quiz/utils/query-keys.ts';

export const useGames = (page = 0, size = 20) => {
  return useQuery({
    queryKey: quizKeys.gamesList(page, size),
    queryFn: () => musicQuizApi.getAllGames(page, size),
  });
};

export const useGame = (gameId: number) => {
  return useQuery({
    queryKey: quizKeys.game(gameId),
    queryFn: () => musicQuizApi.getGameWithGenerations(gameId),
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
