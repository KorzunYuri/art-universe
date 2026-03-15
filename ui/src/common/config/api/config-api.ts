import { ConfigServiceConfig } from '../config/configServiceConfig';

export interface PropertyResponse {
    key: string;
    propertyType: 'INTEGER' | 'BOOLEAN' | 'DECIMAL' | 'STRING';
    currentValue: string;
    defaultValue: string;
    description: string;
}

export async function fetchAllProperties(): Promise<PropertyResponse[]> {
    const response = await ConfigServiceConfig.api.get<PropertyResponse[]>('/properties');
    return response.data;
}

export async function updateProperty(key: string, value: string): Promise<PropertyResponse> {
    const response = await ConfigServiceConfig.api.put<PropertyResponse>(`/properties/${key}`, { value });
    return response.data;
}
