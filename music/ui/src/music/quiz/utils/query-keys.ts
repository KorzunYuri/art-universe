/**
 * Query keys for music-quiz module
 */
export const quizKeys = {
  all: ['quiz'] as const,
  games: () => [...quizKeys.all, 'games'] as const,
  gamesList: (page?: number, size?: number) => 
    [...quizKeys.games(), { page, size }] as const,
  game: (gameId: number) => 
    [...quizKeys.all, 'game', gameId] as const,
  generation: (generationId: number) => 
    [...quizKeys.all, 'generation', generationId] as const,
  generationTracks: (generationId: number) => 
    [...quizKeys.generation(generationId), 'tracks'] as const,
  pipeline: (pipelineId: number) =>
    [...quizKeys.all, 'pipeline', pipelineId] as const,
  generations: (gameId: number) =>
    [...quizKeys.all, 'generations', gameId] as const,
  stepPreview: (stepId: number) =>
    [...quizKeys.all, 'step', stepId, 'preview'] as const,
};
