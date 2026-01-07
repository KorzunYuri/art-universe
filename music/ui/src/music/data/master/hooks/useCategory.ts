import { useQuery } from '@tanstack/react-query';
import { masterEntitiesKeys } from '@/music/shared/utils/query-keys.ts';
import { MusicDataConfig } from '../config/musicdataconfig.ts';
import axios from 'axios';
import type { CategoryDto } from '../api/music-data-categories.ts';

export function useCategory(categoryId: number) {
  return useQuery({
    queryKey: masterEntitiesKeys.detail('category', categoryId),
    queryFn: async (): Promise<CategoryDto> => {
      const response = await axios.get<CategoryDto>(
        `${MusicDataConfig.baseApiUrl}/categories/${categoryId}`
      );
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
}
