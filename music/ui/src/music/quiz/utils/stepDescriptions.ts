import type { PipelineStepType } from '@/music/quiz/types/pipeline-steps.ts';

export const STEP_DESCRIPTIONS: Record<PipelineStepType, string> = {
  'START_DATASOURCE': 'Loads tracks from a specified data source as the initial input for the pipeline.',
  'APPROVED_FILTER': 'Filters the track list to include only tracks that have been manually approved.',
  'BLACKLIST_FILTER': 'Removes tracks belonging to specific categories from the track list.',
  'WHITELIST_FILTER': 'Keeps only tracks belonging to specific categories, removing all others.',
  'TRACK_RECENCY_PENALTY': 'Applies a penalty to tracks that were recently played, reducing their selection probability.',
  'ARTIST_RECENCY_PENALTY': 'Applies a penalty to tracks from artists that were recently played, reducing their selection probability.',
  'ARTIST_DIVERSITY': 'Ensures variety by limiting the number of tracks from the same artist in the final selection.',
  'FINAL_LIMITER': 'Limits the final track list to a specific target number of tracks.',
  'FINAL_CATEGORIES_BALANCER': 'Balances the final track list to achieve a desired distribution across specified categories.',
};

export function getStepDescription(type: PipelineStepType): string {
  return STEP_DESCRIPTIONS[type] || 'No description available for this step type.';
}
