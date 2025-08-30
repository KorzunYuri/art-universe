export interface GameDto {
  id: number;
  generationId: number;
  createdAt: string;
}

export interface GameWithGenerationsDto {
  id: number;
  generationId: number;
  createdAt: string;
  generations: GenerationDto[];
}

export interface GenerationDto {
  id: number;
  gameId: number;
  targetCount: number;
  status: string;
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
