import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { musicQuizApi } from '../api/musicQuizApi';
import { quizKeys } from '../utils/query-keys';

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
    mutationFn: ({ gameId, targetCount }: { gameId: number; targetCount: number }) =>
      musicQuizApi.generateTracks(gameId, targetCount),
    onSuccess: (_, { gameId }) => {
      queryClient.invalidateQueries({ queryKey: quizKeys.game(gameId) });
    },
  });
};

export const useApproveGeneration = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ gameId, generationId }: { gameId: number; generationId: number }) =>
      musicQuizApi.approveGeneration(gameId, generationId),
    onSuccess: (_, { gameId }) => {
      queryClient.invalidateQueries({ queryKey: quizKeys.game(gameId) });
      queryClient.invalidateQueries({ queryKey: quizKeys.games() });
    },
  });
};
