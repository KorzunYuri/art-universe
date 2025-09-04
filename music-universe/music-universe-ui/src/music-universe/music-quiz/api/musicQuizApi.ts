import axios from 'axios';
import type {GameDto, GenerationDto, GenerationTrackDto, Page, GameWithGenerationsDto} from '../types';

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

  getGameWithGenerations: async (gameId: number): Promise<GameWithGenerationsDto> => {
    const response = await api.get(`/games/${gameId}`);
    return response.data;
  },

  createGame: async (): Promise<GameDto> => {
    const response = await api.post('/games');
    return response.data;
  },

  generateTracks: async (gameId: number, targetCount: number): Promise<GenerationDto> => {
    const response = await api.post(`/games/${gameId}/generations`, { targetCount });
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
};
