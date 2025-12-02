import axios from 'axios';
import type {GameDto, GenerationDto, GenerationTrackDto, Page} from '../types';
import type { PipelineDto, PipelineStepDto, GameWithPipelineDto } from '../types/pipeline-steps.ts';

const API_BASE_URL = `http://${import.meta.env.VITE_MU_QUIZ_APP_HOST || 'localhost'}:${import.meta.env.VITE_MU_QUIZ_APP_EXTERNAL_PORT || '7083'}`;

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const musicQuizApi = {
  getAllGames: async (page = 0, size = 20): Promise<Page<GameDto>> => {
    const response = await api.get(`/games?page=${page}&size=${size}`);
    return response.data;
  },

  getGame: async (gameId: number): Promise<GameWithPipelineDto> => {
    const response = await api.get(`/games/${gameId}`);
    return response.data;
  },

  createGame: async (): Promise<GameDto> => {
    const response = await api.post('/games');
    return response.data;
  },

  generateTracks: async (gameId: number, steps?: any[]): Promise<GenerationDto> => {
    const response = await api.post(`/games/${gameId}/generations`, { 
      steps: steps || []
    });
    return response.data;
  },

  getGenerations: async (gameId: number): Promise<GenerationDto[]> => {
    const response = await api.get(`/games/${gameId}/generations`);
    return response.data;
  },

  getGenerationTracks: async (generationId: number): Promise<GenerationTrackDto[]> => {
    const response = await api.get(`/generations/${generationId}/tracks`);
    return response.data;
  },

  approveGeneration: async (generationId: number): Promise<GenerationDto> => {
    const response = await api.patch(`/generations/${generationId}/approve`);
    return response.data;
  },

  disapproveGeneration: async (generationId: number): Promise<GenerationDto> => {
    const response = await api.patch(`/generations/${generationId}/disapprove`);
    return response.data;
  },

  deleteGenerationTrack: async (generationId: number, trackId: number): Promise<void> => {
    await api.delete(`/generations/${generationId}/tracks/${trackId}`);
  },

  // Pipeline management
  getPipeline: async (pipelineId: number): Promise<PipelineDto> => {
    const response = await api.get(`/pipeline/${pipelineId}`);
    return response.data;
  },

  addStep: async (pipelineId: number, stepDto: PipelineStepDto, position: number): Promise<PipelineDto> => {
    const response = await api.post(`/pipeline/${pipelineId}/steps?position=${position}`, stepDto);
    return response.data;
  },

  moveStep: async (pipelineId: number, stepId: number, newPosition: number): Promise<PipelineDto> => {
    const response = await api.put(`/pipeline/${pipelineId}/steps/${stepId}/move?newPosition=${newPosition}`);
    return response.data;
  },

  removeStep: async (pipelineId: number, stepId: number): Promise<PipelineDto> => {
    const response = await api.delete(`/pipeline/${pipelineId}/steps/${stepId}`);
    return response.data;
  },

  updateStepConfiguration: async (pipelineId: number, stepId: number, stepDto: PipelineStepDto): Promise<PipelineDto> => {
    const response = await api.put(`/pipeline/${pipelineId}/steps/${stepId}`, stepDto);
    return response.data;
  },

  getStepPreview: async (stepId: number): Promise<string> => {
    const response = await api.get(`/steps/${stepId}/preview`);
    return response.data;
  },

  executeStep: async (pipelineId: number, stepId: number): Promise<PipelineDto> => {
    const response = await api.post(`/pipeline/${pipelineId}/steps/${stepId}/execute`);
    return response.data;
  },
};
