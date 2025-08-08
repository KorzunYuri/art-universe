import axios from 'axios';
import { MusicQuizConfig } from '../config/musicquizconfig';
import type { MusicQuizSupportedEntityType } from '../types/music-quiz-entity';

/**
 * DTO for quiz binding response
 */
export interface QuizBindingDto {
    masterId: number;
    isBound: boolean;
    bindingId: number | null; // null if not bound
}

/**
 * Map of entity types to their API endpoints
 */
const entityToEndpoint: Record<MusicQuizSupportedEntityType, string> = {
    'artist': 'artists',
    'track': 'tracks'
};

/**
 * Binds a master entity to quiz module
 * 
 * @param entityType The type of entity (artist or track)
 * @param masterId The master entity ID to bind
 * @returns Quiz binding information
 */
export async function bindMasterEntityToQuiz<T extends MusicQuizSupportedEntityType>(
    entityType: T,
    masterId: number
): Promise<QuizBindingDto> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.post<QuizBindingDto>(
        `${MusicQuizConfig.baseApiUrl}/${endpoint}/${masterId}/bind`
    );
    
    return response.data;
}

/**
 * Unbinds a master entity from quiz module
 * 
 * @param entityType The type of entity (artist or track)
 * @param masterId The master entity ID to unbind
 * @returns Quiz binding information
 */
export async function unbindMasterEntityFromQuiz<T extends MusicQuizSupportedEntityType>(
    entityType: T,
    masterId: number
): Promise<QuizBindingDto> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.delete<QuizBindingDto>(
        `${MusicQuizConfig.baseApiUrl}/${endpoint}/${masterId}/bind`
    );
    
    return response.data;
}

/**
 * Gets quiz binding status for a single master entity
 * 
 * @param entityType The type of entity (artist or track)
 * @param masterId The master entity ID to check
 * @returns Quiz binding information
 */
export async function getQuizBinding<T extends MusicQuizSupportedEntityType>(
    entityType: T,
    masterId: number
): Promise<QuizBindingDto> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.get<QuizBindingDto>(
        `${MusicQuizConfig.baseApiUrl}/${endpoint}/${masterId}/binding`
    );
    
    return response.data;
}

/**
 * Gets quiz binding statuses for multiple master entities
 * 
 * @param entityType The type of entity (artist or track)
 * @param masterIds List of master entity IDs to check
 * @returns List of quiz binding information
 */
export async function getQuizBindings<T extends MusicQuizSupportedEntityType>(
    entityType: T,
    masterIds: number[]
): Promise<QuizBindingDto[]> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.post<QuizBindingDto[]>(
        `${MusicQuizConfig.baseApiUrl}/${endpoint}/bindings`,
        masterIds
    );
    
    return response.data;
}
