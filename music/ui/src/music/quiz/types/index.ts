export interface GameDto {
  id: number;
  createdAt: string;
}

export interface GameWithGenerationsDto {
  id: number;
  createdAt: string;
  generations: GenerationDto[];
}

export interface GenerationDto {
  id: number;
  gameId: number;
  pipelineId: number;
  targetCount: number;
  status: string;
  approved: boolean;
  resultTableName: string;
  createdAt: string;
}

export interface GenerationTrackDto {
  trackId: number;
  trackName: string;
  artistName: string;
  orderIndex: number;
}

export interface CreateGenerationRequest {
  targetCount: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
