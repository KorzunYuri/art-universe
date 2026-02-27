import { ArtDataConfig } from '../config/artdataconfig.ts';
import type { Page, BasePageSearchParams } from '@/shared/types/page.ts';

const artDataApi = ArtDataConfig.api;

export interface PersonDto {
    id: number;
    name: string;
}

export interface PersonSaveRequest {
    id?: number;
    name: string;
}

export interface PersonPageSearchParams extends BasePageSearchParams {}

/**
 * Fetches a page of persons
 */
export async function fetchPersons(params: PersonPageSearchParams): Promise<Page<PersonDto>> {
    const response = await artDataApi.get<Page<PersonDto>>('/persons', { params });
    return response.data;
}

/**
 * Fetches a single person by ID
 */
export async function fetchPerson(personId: number): Promise<PersonDto> {
    const response = await artDataApi.get<PersonDto>(`/persons/${personId}`);
    return response.data;
}

/**
 * Creates a new person
 */
export async function createPerson(request: PersonSaveRequest): Promise<PersonDto> {
    const response = await artDataApi.post<PersonDto>('/persons', request);
    return response.data;
}

/**
 * Saves an existing person
 */
export async function savePerson(request: PersonSaveRequest): Promise<PersonDto> {
    const response = await artDataApi.post<PersonDto>('/persons', request);
    return response.data;
}

/**
 * Deletes a person
 */
export async function deletePerson(personId: number): Promise<boolean> {
    const response = await artDataApi.delete<boolean>(`/persons/${personId}`);
    return response.data;
}
